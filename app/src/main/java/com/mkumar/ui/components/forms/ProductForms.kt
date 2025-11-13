package com.mkumar.ui.components.forms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mkumar.data.ProductFormData
import com.mkumar.ui.components.inputs.FieldMode
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
            initialData ?: ProductFormData.FrameData()
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
            mode = FieldMode.TitleCase(),
            onValueChange = { frame = frame.copy(productOwner = it) },
            onCommit = { onChange(frame) }
        )

        OLTextField(
            value = frame.productDescription,
            label = "Description",
            mode = FieldMode.TitleCase(),
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
            initialData ?: ProductFormData.LensData()
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
            mode = FieldMode.TitleCase(),
            onValueChange = { lens = lens.copy(productOwner = it) },
            onCommit = { onChange(lens) }
        )
        OLTextField(
            value = lens.productDescription,
            label = "Description",
            mode = FieldMode.TitleCase(),
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
    var form by remember { mutableStateOf(initialData ?: ProductFormData.ContactLensData()) }

    LaunchedEffect(Unit) {
        snapshotFlow { form }
            .debounce(200)
            .distinctUntilChanged()
            .collect { onChange(it) }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OLTextField(
            value = form.productOwner,
            label = "Product Owner",
            mode = FieldMode.TitleCase(),
            onValueChange = { form = form.copy(productOwner = it) },
            onCommit = { onChange(form) }
        )
        OLTextField(
            value = form.productDescription,
            label = "Description",
            mode = FieldMode.TitleCase(),
            onValueChange = { form = form.copy(productDescription = it) },
            onCommit = { onChange(form) }
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OLTextField(
                value = form.rightSph,
                label = "Right Sph",
                placeholder = "+1.50",
                mode = FieldMode.SignedDecimal(scale = 2, forcePlus = true),
                onValueChange = { form = form.copy(rightSph = it) },
                modifier = Modifier.weight(1f),
                onCommit = { onChange(form) } // still emits when blurred/done/next
            )
            OLTextField(
                value = form.rightCyl,
                label = "Right Cyl",
                placeholder = "-0.50",
                mode = FieldMode.SignedDecimal(scale = 2, forcePlus = false),
                onValueChange = { form = form.copy(rightCyl = it) },
                modifier = Modifier.weight(1f),
                onCommit = { onChange(form) }
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OLTextField(
                value = form.rightAxis,
                label = "Right Axis",
                placeholder = "175",
                mode = FieldMode.AxisDegrees,        // clamps 0..180 on commit
                onValueChange = { form = form.copy(rightAxis = it) },
                modifier = Modifier.weight(1f),
                onCommit = { onChange(form) }
            )
            OLTextField(
                value = form.rightAdd,
                label = "Right Add",
                placeholder = "+1.75",
                mode = FieldMode.SignedDecimal(scale = 2, forcePlus = true),
                onValueChange = { form = form.copy(rightAdd = it) },
                modifier = Modifier.weight(1f),
                onCommit = { onChange(form) }
            )
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OLTextField(
                value = form.leftSph,
                label = "Left Sph",
                placeholder = "+1.50",
                mode = FieldMode.SignedDecimal(scale = 2, forcePlus = true),
                onValueChange = { form = form.copy(leftSph = it) },
                modifier = Modifier.weight(1f),
                onCommit = { onChange(form) } // still emits when blurred/done/next
            )
            OLTextField(
                value = form.leftCyl,
                label = "Left Cyl",
                placeholder = "-0.50",
                mode = FieldMode.SignedDecimal(scale = 2, forcePlus = false),
                onValueChange = { form = form.copy(leftCyl = it) },
                modifier = Modifier.weight(1f),
                onCommit = { onChange(form) }
            )
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OLTextField(
                value = form.leftAxis,
                label = "Left Axis",
                placeholder = "175",
                mode = FieldMode.AxisDegrees,        // clamps 0..180 on commit
                onValueChange = { form = form.copy(leftAxis = it) },
                modifier = Modifier.weight(1f),
                onCommit = { onChange(form) }
            )
            OLTextField(
                value = form.leftAdd,
                label = "Left Add",
                placeholder = "+1.75",
                mode = FieldMode.SignedDecimal(scale = 2, forcePlus = true),
                onValueChange = { form = form.copy(leftAdd = it) },
                modifier = Modifier.weight(1f),
                onCommit = { onChange(form) }
            )
        }

        // Pricing block
        ItemPriceEditor(
            initialUnitPrice = form.unitPrice.toString(),
            initialDiscountPct = form.discountPct.toString(),
            initialQuantity = form.quantity.toString(),
            onUnitPriceChange = { form = form.copy(unitPrice = it.toIntOrNull() ?: 0) },
            onDiscountChange = { form = form.copy(discountPct = it.toIntOrNull() ?: 0) },
            onQuantityChange = { form = form.copy(quantity = it.toIntOrNull() ?: 1) },
            onTotalChange = { form = form.copy(total = it.toIntOrNull() ?: 0) }
        )
    }
}

@OptIn(FlowPreview::class)
@Composable
fun GeneralProductForm(
    initialData: ProductFormData.GeneralProductData? = null,
    onChange: (ProductFormData.GeneralProductData) -> Unit,
) {
    var form by remember {
        mutableStateOf(
            initialData ?: ProductFormData.GeneralProductData()
        )
    }

    LaunchedEffect(Unit) {
        snapshotFlow { form }
            .debounce(200)
            .distinctUntilChanged()
            .collect { onChange(it) }
    }

    Column {
        OLTextField(
            value = form.productOwner,
            label = "Product Owner",
            mode = FieldMode.TitleCase(),
            onValueChange = { form = form.copy(productOwner = it) },
            onCommit = { onChange(form) }
        )
        OLTextField(
            value = form.productDescription,
            label = "Description",
            mode = FieldMode.TitleCase(),
            onValueChange = { form = form.copy(productDescription = it) },
            onCommit = { onChange(form) }
        )
        OLTextField(
            value = form.productType,
            label = "Product Type",
            mode = FieldMode.TitleCase(),
            onValueChange = { form = form.copy(productType = it) },
            onCommit = { onChange(form) }
        )

        ItemPriceEditor(
            initialUnitPrice = form.unitPrice.toString(),
            initialDiscountPct = form.discountPct.toString(),
            initialQuantity = form.quantity.toString(),
            onUnitPriceChange = { form = form.copy(unitPrice = it.toIntOrNull() ?: 0) },
            onDiscountChange = { form = form.copy(discountPct = it.toIntOrNull() ?: 0) },
            onQuantityChange = { form = form.copy(quantity = it.toIntOrNull() ?: 1) },
            onTotalChange = { form = form.copy(total = it.toIntOrNull() ?: 0) }
        )
    }
}