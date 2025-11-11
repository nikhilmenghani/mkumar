package com.mkumar.ui.components.forms

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.mkumar.data.ProductFormData
import com.mkumar.ui.components.inputs.ItemPriceEditor
import com.mkumar.ui.components.inputs.OLTextField

@Composable
fun FrameForm(
    initialData: ProductFormData.FrameData? = null,
    onChange: (ProductFormData.FrameData) -> Unit,
) {
    var brand by remember { mutableStateOf(initialData?.brand.orEmpty()) }
    var color by remember { mutableStateOf(initialData?.color.orEmpty()) }
    var size by remember { mutableStateOf(initialData?.size.orEmpty()) }
    var unitPrice by remember { mutableStateOf(initialData?.unitPrice?.toString() ?: "") }
    var discountPct by remember { mutableStateOf(initialData?.discountPct?.toString() ?: "0") }
    var quantity by remember { mutableStateOf(initialData?.quantity?.toString() ?: "1") }
    var total by remember { mutableStateOf(initialData?.total?.toString() ?: "0") }
    var description by remember { mutableStateOf(initialData?.productDescription.orEmpty()) }

    Column {

        OLTextField(
            value = description,
            label = "Description",
            onCommit = {
                onChange(
                    ProductFormData.FrameData(
                        productOwner = initialData?.productOwner.orEmpty(),
                        productDescription = description,
                        brand = brand,
                        color = color,
                        size = size,
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
                    ProductFormData.FrameData(
                        productOwner = initialData?.productOwner.orEmpty(),
                        productDescription = description,
                        brand = brand,
                        color = color,
                        size = size,
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
                    ProductFormData.FrameData(
                        productOwner = initialData?.productOwner.orEmpty(),
                        productDescription = description,
                        brand = brand,
                        color = color,
                        size = size,
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
                    ProductFormData.FrameData(
                        productOwner = initialData?.productOwner.orEmpty(),
                        productDescription = description,
                        brand = brand,
                        color = color,
                        size = size,
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
                    ProductFormData.FrameData(
                        productOwner = initialData?.productOwner.orEmpty(),
                        brand = brand,
                        color = color,
                        size = size,
                        productDescription = description,
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
fun LensForm(
    initialData: ProductFormData.LensData? = null,
    onChange: (ProductFormData.LensData) -> Unit,
) {
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
    var power by remember { mutableStateOf(initialData?.power.orEmpty()) }
    var duration by remember { mutableStateOf(initialData?.duration.orEmpty()) }
    var unitPrice by remember { mutableStateOf(initialData?.unitPrice?.toString() ?: "0") }
    var discountPct by remember { mutableStateOf(initialData?.discountPct?.toString() ?: "0") }
    var quantity by remember { mutableStateOf(initialData?.quantity?.toString() ?: "1") }
    var total by remember { mutableStateOf(initialData?.total?.toString() ?: "0") }
    var description by remember { mutableStateOf(initialData?.productDescription.orEmpty()) }

    Column {
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



