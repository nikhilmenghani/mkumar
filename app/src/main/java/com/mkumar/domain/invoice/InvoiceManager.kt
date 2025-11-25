package com.mkumar.domain.invoice

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.mkumar.common.constant.CustomerDetailsConstants
import com.mkumar.common.extension.DateFormat
import com.mkumar.common.extension.formatAsDate
import com.mkumar.common.files.saveInvoicePdf
import com.mkumar.data.ProductFormData
import com.mkumar.domain.logo.LogoProvider
import com.mkumar.domain.pricing.PricingInput
import com.mkumar.domain.pricing.PricingService
import com.mkumar.model.UiOrderItem
import com.mkumar.model.productTypeLabelDisplayNames
import com.mkumar.repository.OrderRepository
import com.mkumar.repository.ProductRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InvoiceManager @Inject constructor(
    private val orderRepo: OrderRepository,
    private val orderItemRepo: ProductRepository,
    private val pricing: PricingService,
    private val logoProvider: LogoProvider,
    @ApplicationContext private val app: Context,
) {
    suspend fun generateInvoicePdf(orderId: String, invoiceNumber: String, logo: Bitmap): Uri? {
        val fileName = CustomerDetailsConstants.getInvoiceFileName(orderId, invoiceNumber, withTimeStamp = true) + ".pdf"

        val order = orderRepo.getOrder(orderId) ?: return null
        val itemEntities = orderItemRepo.getItemsForOrder(orderId)
        val customer = orderRepo.getCustomerMiniForOrder(order.customerId)

        val input = PricingInput(
            orderId = order.id,
            items = itemEntities.map {
                PricingInput.ItemInput(
                    itemId = it.id,
                    quantity = it.quantity,
                    unitPrice = it.unitPrice,
                    discountPercentage = it.discountPercentage
                )
            },
            adjustedAmount = order.adjustedAmount.coerceAtLeast(0),
            paidTotal = order.paidTotal.coerceAtLeast(0)
        )

        val priced = pricing.price(input)
        val pricedById = priced.items.associateBy { it.itemId }

        val invoiceItems: List<InvoiceItemRow> = itemEntities.map { e ->
            val p = pricedById[e.id]
            val lineTotal = p?.lineTotal ?: e.finalTotal
            val description = UiOrderItem.deserializeFormData(e.formDataJson)?.productDescription ?: ""
            val productType = if (e.productTypeLabel == "GeneralProduct") {
                (UiOrderItem.deserializeFormData(e.formDataJson) as? ProductFormData.GeneralProductData)?.productType
                    ?: productTypeLabelDisplayNames[e.productTypeLabel] ?: "Unknown"
            } else {
                productTypeLabelDisplayNames[e.productTypeLabel] ?: "Unknown"
            }
            InvoiceItemRow(
                name = customer.name,
                qty = e.quantity,
                unitPrice = e.unitPrice.toDouble(),
                total = lineTotal.toDouble(),
                discount = e.discountPercentage,
                description = description,
                owner = e.productOwnerName,
                productType = productType
            )
        }

        val invoiceData = InvoiceData(
            shopName = "M Kumar Luxurious Watch & Optical Store",
            shopAddress = "7, Shlok Height, Opp. Dev Paradise & Dharti Silver, Nr. Mansarovar Road, Chandkheda, Ahmedabad.",
            customerName = customer.name,
            ownerName = "Mahendra Menghani",
            customerPhone = customer.phone,
            ownerPhone = "942795 6490",
            ownerEmail = "menghani.mahendra@gmail.com",
            orderId = order.id,
            invoiceNumber = order.invoiceSeq.toString(),
            occurredAtText = order.occurredAt.formatAsDate(format = DateFormat.DEFAULT_DATE_TIME),
            items = invoiceItems,
            subtotal = priced.subtotalBeforeAdjust.toDouble(),
            adjustedTotal = priced.adjustedAmount.toDouble(),
            paidTotal = priced.paidTotal.toDouble(),
            remainingBalance = priced.remainingBalance.toDouble(),
            logoBitmap = logo
        )

        val bytes = InvoicePdfBuilderImpl().build(invoiceData)
        return saveInvoicePdf(app, fileName, bytes)
    }

    sealed class InvoiceResult {
        data class Success(val uri: Uri) : InvoiceResult()
        data class NotFound(val message: String) : InvoiceResult()
        data class Error(val throwable: Throwable) : InvoiceResult()
    }

    suspend fun createInvoice(orderId: String, invoiceNumber: String): InvoiceResult {
        return try {
            val logo = logoProvider.getLogo()
            val uri = generateInvoicePdf(orderId, invoiceNumber, logo)
            if (uri != null) InvoiceResult.Success(uri)
            else InvoiceResult.NotFound("Order not found.")
        } catch (t: Throwable) {
            InvoiceResult.Error(t)
        }
    }
}
