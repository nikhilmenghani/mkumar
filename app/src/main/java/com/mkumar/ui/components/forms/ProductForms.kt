package com.mkumar.ui.components.forms

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import com.mkumar.data.ProductFormData
import com.mkumar.ui.components.inputs.ItemPriceEditor
import com.mkumar.ui.components.inputs.OLTextField
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(FlowPreview::class)
@Composable
fun FrameForm(
    initialData: ProductFormData.FrameData? = null,
    onChange: (ProductFormData.FrameData) -> Unit,
) {
    var frame by remember {
        mutableStateOf(
            initialData ?: ProductFormData.FrameData(
                productOwner = "",
                productDescription = "",
                brand = "",
                color = "",
                size = "",
                unitPrice = 0,
                discountPct = 0,
                quantity = 1,
                total = 0
            )
        )
    }

    LaunchedEffect(Unit) {
        snapshotFlow { frame }
            .debounce(200)
            .distinctUntilChanged()
            .collect { onChange(it) }
    }

    // Throttle expensive upstream updates
    LaunchedEffect(Unit) {
        snapshotFlow { frame }
            .debounce(200)
            .distinctUntilChanged()
            .collect { onChange(it) }
    }

    Column {
        OLTextField(
            value = frame.productOwner,
            label = "Product Owner",
            onValueChange = { frame = frame.copy(productOwner = it) },
            onCommit = { onChange(frame) }
        )

        OLTextField(
            value = frame.productDescription,
            label = "Description",
            onValueChange = { frame = frame.copy(productDescription = it) },
            onCommit = { onChange(frame) }
        )

        ItemPriceEditor(
            initialUnitPrice = frame.unitPrice.toString(),
            initialDiscountPct = frame.discountPct.toString(),
            initialQuantity = frame.quantity.toString(),
            onUnitPriceChange = { frame = frame.copy(unitPrice = it.toIntOrNull() ?: 0) },
            onDiscountChange = { frame = frame.copy(discountPct = it.toIntOrNull() ?: 0) },
            onQuantityChange = { frame = frame.copy(quantity = it.toIntOrNull() ?: 1) },
            onTotalChange = { frame = frame.copy(total = it.toIntOrNull() ?: 0) }
        )
    }
}

@Composable
fun LensForm(
    initialData: ProductFormData.LensData? = null,
    onChange: (ProductFormData.LensData) -> Unit,
) {
    var owner by remember { mutableStateOf(initialData?.productOwner.orEmpty()) }
    var leftSphere by remember { mutableStateOf(initialData?.leftSphere.orEmpty()) }
    var leftAxis by remember { mutableStateOf(initialData?.leftAxis.orEmpty()) }
    var rightSphere by remember { mutableStateOf(initialData?.rightSphere.orEmpty()) }
    var rightAxis by remember { mutableStateOf(initialData?.rightAxis.orEmpty()) }
    var unitPrice by remember { mutableStateOf(initialData?.unitPrice?.toString() ?: "") }
    var discountPct by remember { mutableStateOf(initialData?.discountPct?.toString() ?: "0") }
    var quantity by remember { mutableStateOf(initialData?.quantity?.toString() ?: "1") }
    var total by remember { mutableStateOf(initialData?.total?.toString() ?: "0") }
    var description by remember { mutableStateOf(initialData?.productDescription.orEmpty()) }

    Column {
        OLTextField(
            value = owner,
            label = "Product Owner",
            onCommit = {
                onChange(
                    ProductFormData.LensData(
                        productOwner = owner,
                        productDescription = description,
                        leftSphere = leftSphere,
                        leftAxis = leftAxis,
                        rightSphere = rightSphere,
                        rightAxis = rightAxis,
                        unitPrice = unitPrice.toIntOrNull() ?: 0,
                        discountPct = discountPct.toIntOrNull() ?: 0,
                        quantity = quantity.toIntOrNull() ?: 1,
                        total = total.toInt()
                    )
                )
            },
            onValueChange = { owner = it }
        )

        OLTextField(
            value = description,
            label = "Description",
            onCommit = {
                onChange(
                    ProductFormData.LensData(
                        productOwner = initialData?.productOwner.orEmpty(),
                        productDescription = description,
                        leftSphere = leftSphere,
                        leftAxis = leftAxis,
                        rightSphere = rightSphere,
                        rightAxis = rightAxis,
                        unitPrice = unitPrice.toIntOrNull() ?: 0,
                        discountPct = discountPct.toIntOrNull() ?: 0,
                        quantity = quantity.toIntOrNull() ?: 1,
                        total = total.toInt()
                    )
                )
            },
            onValueChange = { description = it }
        )

        ItemPriceEditor(
            initialUnitPrice = unitPrice,
            initialDiscountPct = discountPct,
            initialQuantity = quantity,
            onUnitPriceChange = { newPrice ->
                unitPrice = newPrice
                onChange(
                    ProductFormData.LensData(
                        productOwner = initialData?.productOwner.orEmpty(),
                        productDescription = description,
                        leftSphere = leftSphere,
                        leftAxis = leftAxis,
                        rightSphere = rightSphere,
                        rightAxis = rightAxis,
                        unitPrice = newPrice.toIntOrNull() ?: 0,
                        discountPct = discountPct.toIntOrNull() ?: 0,
                        quantity = quantity.toIntOrNull() ?: 1,
                        total = total.toInt()
                    )
                )
            },
            onDiscountChange = { newDiscount ->
                discountPct = newDiscount
                onChange(
                    ProductFormData.LensData(
                        productOwner = initialData?.productOwner.orEmpty(),
                        productDescription = description,
                        leftSphere = leftSphere,
                        leftAxis = leftAxis,
                        rightSphere = rightSphere,
                        rightAxis = rightAxis,
                        unitPrice = unitPrice.toIntOrNull() ?: 0,
                        discountPct = newDiscount.toIntOrNull() ?: 0,
                        quantity = quantity.toIntOrNull() ?: 1,
                        total = total.toInt()
                    )
                )
            },
            onQuantityChange = { newQuantity ->
                quantity = newQuantity
                onChange(
                    ProductFormData.LensData(
                        productOwner = initialData?.productOwner.orEmpty(),
                        productDescription = description,
                        leftSphere = leftSphere,
                        leftAxis = leftAxis,
                        rightSphere = rightSphere,
                        rightAxis = rightAxis,
                        unitPrice = unitPrice.toIntOrNull() ?: 0,
                        discountPct = discountPct.toIntOrNull() ?: 0,
                        quantity = newQuantity.toIntOrNull() ?: 1,
                        total = total.toInt()
                    )
                )
            },
            onTotalChange = { newTotal ->
                total = newTotal
                onChange(
                    ProductFormData.LensData(
                        productOwner = initialData?.productOwner.orEmpty(),
                        productDescription = description,
                        leftSphere = leftSphere,
                        leftAxis = leftAxis,
                        rightSphere = rightSphere,
                        rightAxis = rightAxis,
                        unitPrice = unitPrice.toIntOrNull() ?: 0,
                        discountPct = discountPct.toIntOrNull() ?: 0,
                        quantity = quantity.toIntOrNull() ?: 1,
                        total = newTotal.toInt()
                    )
                )
            }
        )
    }
}

@Composable
fun ContactLensForm(
    initialData: ProductFormData.ContactLensData? = null,
    onChange: (ProductFormData.ContactLensData) -> Unit,
) {
    var owner by remember { mutableStateOf(initialData?.productOwner.orEmpty()) }
    var power by remember { mutableStateOf(initialData?.power.orEmpty()) }
    var duration by remember { mutableStateOf(initialData?.duration.orEmpty()) }
    var unitPrice by remember { mutableStateOf(initialData?.unitPrice?.toString() ?: "0") }
    var discountPct by remember { mutableStateOf(initialData?.discountPct?.toString() ?: "0") }
    var quantity by remember { mutableStateOf(initialData?.quantity?.toString() ?: "1") }
    var total by remember { mutableStateOf(initialData?.total?.toString() ?: "0") }
    var description by remember { mutableStateOf(initialData?.productDescription.orEmpty()) }

    Column {
        OLTextField(
            value = owner,
            label = "Product Owner",
            onCommit = {
                onChange(
                    ProductFormData.ContactLensData(
                        productOwner = owner,
                        productDescription = description,
                        power = power,
                        duration = duration,
                        unitPrice = unitPrice.toIntOrNull() ?: 0,
                        discountPct = discountPct.toIntOrNull() ?: 0,
                        quantity = quantity.toIntOrNull() ?: 1,
                        total = total.toInt()
                    )
                )
            },
            onValueChange = { owner = it }
        )

        OLTextField(
            value = description,
            label = "Description",
            onCommit = {
                onChange(
                    ProductFormData.ContactLensData(
                        productOwner = initialData?.productOwner.orEmpty(),
                        productDescription = description,
                        power = power,
                        duration = duration,
                        unitPrice = unitPrice.toIntOrNull() ?: 0,
                        discountPct = discountPct.toIntOrNull() ?: 0,
                        quantity = quantity.toIntOrNull() ?: 1,
                        total = total.toInt()
                    )
                )
            },
            onValueChange = { description = it }
        )

        ItemPriceEditor(
            initialUnitPrice = unitPrice,
            initialDiscountPct = discountPct,
            initialQuantity = quantity,
            onUnitPriceChange = { newPrice ->
                unitPrice = newPrice
                onChange(
                    ProductFormData.ContactLensData(
                        productOwner = initialData?.productOwner.orEmpty(),
                        productDescription = description,
                        power = power,
                        duration = duration,
                        unitPrice = newPrice.toIntOrNull() ?: 0,
                        discountPct = discountPct.toIntOrNull() ?: 0,
                        quantity = quantity.toIntOrNull() ?: 1,
                        total = total.toInt()
                    )
                )
            },
            onDiscountChange = { newDiscount ->
                discountPct = newDiscount
                onChange(
                    ProductFormData.ContactLensData(
                        productOwner = initialData?.productOwner.orEmpty(),
                        productDescription = description,
                        power = power,
                        duration = duration,
                        unitPrice = unitPrice.toIntOrNull() ?: 0,
                        discountPct = newDiscount.toIntOrNull() ?: 0,
                        quantity = quantity.toIntOrNull() ?: 1,
                        total = total.toInt()
                    )
                )
            },
            onQuantityChange = { newQuantity ->
                quantity = newQuantity
                onChange(
                    ProductFormData.ContactLensData(
                        productOwner = initialData?.productOwner.orEmpty(),
                        productDescription = description,
                        power = power,
                        duration = duration,
                        unitPrice = unitPrice.toIntOrNull() ?: 0,
                        discountPct = discountPct.toIntOrNull() ?: 0,
                        quantity = newQuantity.toIntOrNull() ?: 1,
                        total = total.toInt()
                    )
                )
            },
            onTotalChange = { newTotal ->
                total = newTotal
                onChange(
                    ProductFormData.ContactLensData(
                        productOwner = initialData?.productOwner.orEmpty(),
                        productDescription = description,
                        power = power,
                        duration = duration,
                        unitPrice = unitPrice.toIntOrNull() ?: 0,
                        discountPct = discountPct.toIntOrNull() ?: 0,
                        quantity = quantity.toIntOrNull() ?: 1,
                        total = newTotal.toInt()
                    )
                )
            }
        )
    }
}



