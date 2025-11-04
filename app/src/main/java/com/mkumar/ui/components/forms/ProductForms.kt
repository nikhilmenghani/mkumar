package com.mkumar.ui.components.forms

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import com.mkumar.data.ProductFormData
import com.mkumar.ui.components.inputs.ItemPriceEditor

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

    Column {
        var isBrandFocused by remember { mutableStateOf(false) }
        OutlinedTextField(
            value = brand,
            onValueChange = { brand = it },
            label = { Text("Brand") },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged {
                    if (isBrandFocused && !it.isFocused) {
                        onChange(
                            ProductFormData.FrameData(
                                brand = brand,
                                color = color,
                                size = size,
                                unitPrice = unitPrice.toIntOrNull() ?: 0,
                                discountPct = discountPct.toIntOrNull() ?: 0,
                                quantity = quantity.toIntOrNull() ?: 1,
                                total = total.toInt()
                            )
                        )
                    }
                    isBrandFocused = it.isFocused
                }
        )

        var isColorFocused by remember { mutableStateOf(false) }
        OutlinedTextField(
            value = color,
            onValueChange = { color = it },
            label = { Text("Color") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .onFocusChanged {
                    if (isColorFocused && !it.isFocused) {
                        onChange(
                            ProductFormData.FrameData(
                                brand = brand,
                                color = color,
                                size = size,
                                unitPrice = unitPrice.toIntOrNull() ?: 0,
                                discountPct = discountPct.toIntOrNull() ?: 0,
                                quantity = quantity.toIntOrNull() ?: 1,
                                total = total.toInt()
                            )
                        )
                    }
                    isColorFocused = it.isFocused
                }
        )

        var isSizeFocused by remember { mutableStateOf(false) }
        OutlinedTextField(
            value = size,
            onValueChange = { size = it },
            label = { Text("Size") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .onFocusChanged {
                    if (isSizeFocused && !it.isFocused) {
                        onChange(
                            ProductFormData.FrameData(
                                brand = brand,
                                color = color,
                                size = size,
                                unitPrice = unitPrice.toIntOrNull() ?: 0,
                                discountPct = discountPct.toIntOrNull() ?: 0,
                                quantity = quantity.toIntOrNull() ?: 1,
                                total = total.toInt()
                            )
                        )
                    }
                    isSizeFocused = it.isFocused
                }
        )

        ItemPriceEditor(
            initialUnitPrice = unitPrice,
            initialDiscountPct = discountPct,
            initialQuantity = quantity,
            onUnitPriceChange = { newPrice ->
                unitPrice = newPrice
                onChange(
                    ProductFormData.FrameData(
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
                        brand = brand,
                        color = color,
                        size = size,
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

    Column {
        var leftSphereFocused by remember { mutableStateOf(false) }
        OutlinedTextField(
            value = leftSphere,
            onValueChange = { leftSphere = it },
            label = { Text("Left Sphere") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .onFocusChanged {
                    if (leftSphereFocused && !it.isFocused) {
                        onChange(
                            ProductFormData.LensData(
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
                    }
                    leftSphereFocused = it.isFocused
                }
        )

        var leftAxisFocused by remember { mutableStateOf(false) }
        OutlinedTextField(
            value = leftAxis,
            onValueChange = { leftAxis = it },
            label = { Text("Left Axis") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .onFocusChanged {
                    if (leftAxisFocused && !it.isFocused) {
                        onChange(
                            ProductFormData.LensData(
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
                    }
                    leftAxisFocused = it.isFocused
                }
        )

        var rightSphereFocused by remember { mutableStateOf(false) }
        OutlinedTextField(
            value = rightSphere,
            onValueChange = { rightSphere = it },
            label = { Text("Right Sphere") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .onFocusChanged {
                    if (rightSphereFocused && !it.isFocused) {
                        onChange(
                            ProductFormData.LensData(
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
                    }
                    rightSphereFocused = it.isFocused
                }
        )

        var rightAxisFocused by remember { mutableStateOf(false) }
        OutlinedTextField(
            value = rightAxis,
            onValueChange = { rightAxis = it },
            label = { Text("Right Axis") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .onFocusChanged {
                    if (rightAxisFocused && !it.isFocused) {
                        onChange(
                            ProductFormData.LensData(
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
                    }
                    rightAxisFocused = it.isFocused
                }
        )

        ItemPriceEditor(
            initialUnitPrice = unitPrice,
            initialDiscountPct = discountPct,
            initialQuantity = quantity,
            onUnitPriceChange = { newPrice ->
                unitPrice = newPrice
                onChange(
                    ProductFormData.LensData(
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

    Column {
        var isPowerFocused by remember { mutableStateOf(false) }
        OutlinedTextField(
            value = power,
            onValueChange = { power = it },
            label = { Text("Power") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .onFocusChanged {
                    if (isPowerFocused && !it.isFocused) {
                        onChange(
                            ProductFormData.ContactLensData(
                                power = power,
                                duration = duration,
                                unitPrice = unitPrice.toIntOrNull() ?: 0,
                                discountPct = discountPct.toIntOrNull() ?: 0,
                                quantity = quantity.toIntOrNull() ?: 1,
                                total = total.toInt()
                            )
                        )
                    }
                    isPowerFocused = it.isFocused
                }
        )

        var isDurationFocused by remember { mutableStateOf(false) }
        OutlinedTextField(
            value = duration,
            onValueChange = { duration = it },
            label = { Text("Duration") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .onFocusChanged {
                    if (isDurationFocused && !it.isFocused) {
                        onChange(
                            ProductFormData.ContactLensData(
                                power = power,
                                duration = duration,
                                unitPrice = unitPrice.toIntOrNull() ?: 0,
                                discountPct = discountPct.toIntOrNull() ?: 0,
                                quantity = quantity.toIntOrNull() ?: 1,
                                total = total.toInt()
                            )
                        )
                    }
                    isDurationFocused = it.isFocused
                }
        )

        ItemPriceEditor(
            initialUnitPrice = unitPrice,
            initialDiscountPct = discountPct,
            initialQuantity = quantity,
            onUnitPriceChange = { newPrice ->
                unitPrice = newPrice
                onChange(
                    ProductFormData.ContactLensData(
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



