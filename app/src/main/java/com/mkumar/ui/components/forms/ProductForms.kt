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

@OptIn(FlowPreview::class)
@Composable
fun LensForm(
    initialData: ProductFormData.LensData? = null,
    onChange: (ProductFormData.LensData) -> Unit,
) {
    var lens by remember {
        mutableStateOf(
            initialData ?: ProductFormData.LensData(
                productOwner = "",
                productDescription = "",
                leftSphere = "",
                leftAxis = "",
                rightSphere = "",
                rightAxis = "",
                unitPrice = 0,
                discountPct = 0,
                quantity = 1,
                total = 0
            )
        )
    }

    LaunchedEffect(Unit) {
        snapshotFlow { lens }
            .debounce(200)
            .distinctUntilChanged()
            .collect { onChange(it) }
    }

    Column {
        OLTextField(
            value = lens.productOwner,
            label = "Product Owner",
            onValueChange = { lens = lens.copy(productOwner = it) },
            onCommit = { onChange(lens) }
        )
        OLTextField(
            value = lens.productDescription,
            label = "Description",
            onValueChange = { lens = lens.copy(productDescription = it) },
            onCommit = { onChange(lens) }
        )

        ItemPriceEditor(
            initialUnitPrice = lens.unitPrice.toString(),
            initialDiscountPct = lens.discountPct.toString(),
            initialQuantity = lens.quantity.toString(),
            onUnitPriceChange = { lens = lens.copy(unitPrice = it.toIntOrNull() ?: 0) },
            onDiscountChange = { lens = lens.copy(discountPct = it.toIntOrNull() ?: 0) },
            onQuantityChange = { lens = lens.copy(quantity = it.toIntOrNull() ?: 1) },
            onTotalChange = { lens = lens.copy(total = it.toIntOrNull() ?: 0) }
        )
    }
}

@OptIn(FlowPreview::class)
@Composable
fun ContactLensForm(
    initialData: ProductFormData.ContactLensData? = null,
    onChange: (ProductFormData.ContactLensData) -> Unit,
) {
    var contactLens by remember {
        mutableStateOf(
            initialData ?: ProductFormData.ContactLensData(
                productOwner = "",
                productDescription = "",
                power = "",
                duration = "",
                unitPrice = 0,
                discountPct = 0,
                quantity = 1,
                total = 0
            )
        )
    }

    LaunchedEffect(Unit) {
        snapshotFlow { contactLens }
            .debounce(200)
            .distinctUntilChanged()
            .collect { onChange(it) }
    }

    Column {
        OLTextField(
            value = contactLens.productOwner,
            label = "Product Owner",
            onValueChange = { contactLens = contactLens.copy(productOwner = it) },
            onCommit = { onChange(contactLens) }
        )
        OLTextField(
            value = contactLens.productDescription,
            label = "Description",
            onValueChange = { contactLens = contactLens.copy(productDescription = it) },
            onCommit = { onChange(contactLens) }
        )

        ItemPriceEditor(
            initialUnitPrice = contactLens.unitPrice.toString(),
            initialDiscountPct = contactLens.discountPct.toString(),
            initialQuantity = contactLens.quantity.toString(),
            onUnitPriceChange = { contactLens = contactLens.copy(unitPrice = it.toIntOrNull() ?: 0) },
            onDiscountChange = { contactLens = contactLens.copy(discountPct = it.toIntOrNull() ?: 0) },
            onQuantityChange = { contactLens = contactLens.copy(quantity = it.toIntOrNull() ?: 1) },
            onTotalChange = { contactLens = contactLens.copy(total = it.toIntOrNull() ?: 0) }
        )
    }
}



