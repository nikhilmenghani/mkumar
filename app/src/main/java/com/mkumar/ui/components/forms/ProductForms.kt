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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mkumar.data.ProductFormData
import com.mkumar.ui.components.headers.FractionedSectionHeader
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
        FractionedSectionHeader("Product Information")
        OLTextField(
            value = frame.productOwner,
            label = "Product Owner",
            mode = FieldMode.TitleCase(),
            modifier = Modifier.fillMaxWidth(),
            onValueChange = { frame = frame.copy(productOwner = it) },
            onCommit = { onChange(frame) }
        )

        OLTextField(
            value = frame.productDescription,
            label = "Description",
            mode = FieldMode.TitleCase(),
            modifier = Modifier.fillMaxWidth(),
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
fun GlassForm(
    initialData: ProductFormData.GlassData? = null,
    onChange: (ProductFormData.GlassData) -> Unit,
) {
    var form by remember {
        mutableStateOf(
            initialData ?: ProductFormData.GlassData()
        )
    }

    LaunchedEffect(Unit) {
        snapshotFlow { form }
            .debounce(200)
            .distinctUntilChanged()
            .collect { onChange(it) }
    }

    Column {
        FractionedSectionHeader("Product Information")
        OLTextField(
            value = form.productOwner,
            label = "Product Owner",
            mode = FieldMode.TitleCase(),
            modifier = Modifier.fillMaxWidth(),
            onValueChange = { form = form.copy(productOwner = it) },
            onCommit = { onChange(form) }
        )
        OLTextField(
            value = form.productDescription,
            label = "Description",
            mode = FieldMode.TitleCase(),
            modifier = Modifier.fillMaxWidth(),
            onValueChange = { form = form.copy(productDescription = it) },
            onCommit = { onChange(form) }
        )
        FractionedSectionHeader("Right Eye")
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OLTextField(
                value = form.rightSph,
                label = "R Sph",
                placeholder = "+1.50",
                mode = FieldMode.SignedDecimal(scale = 2, forcePlus = true),
                onValueChange = { form = form.copy(rightSph = it) },
                modifier = Modifier.weight(1f),
                onCommit = { onChange(form) } // still emits when blurred/done/next
            )
            OLTextField(
                value = form.rightCyl,
                label = "R Cyl",
                placeholder = "-0.50",
                mode = FieldMode.SignedDecimal(scale = 2, forcePlus = false),
                onValueChange = { form = form.copy(rightCyl = it) },
                modifier = Modifier.weight(1f),
                onCommit = { onChange(form) }
            )
            OLTextField(
                value = form.rightAxis,
                label = "R Axis",
                placeholder = "175",
                mode = FieldMode.AxisDegrees,        // clamps 0..180 on commit
                onValueChange = { form = form.copy(rightAxis = it) },
                modifier = Modifier.weight(1f),
                onCommit = { onChange(form) }
            )
            OLTextField(
                value = form.rightAdd,
                label = "R Add",
                placeholder = "+1.75",
                mode = FieldMode.SignedDecimal(scale = 2, forcePlus = true),
                onValueChange = { form = form.copy(rightAdd = it) },
                modifier = Modifier.weight(1f),
                onCommit = { onChange(form) }
            )
        }

        FractionedSectionHeader("Left Eye")

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OLTextField(
                value = form.leftSph,
                label = "L Sph",
                placeholder = "+1.50",
                mode = FieldMode.SignedDecimal(scale = 2, forcePlus = true),
                onValueChange = { form = form.copy(leftSph = it) },
                modifier = Modifier.weight(1f),
                onCommit = { onChange(form) } // still emits when blurred/done/next
            )
            OLTextField(
                value = form.leftCyl,
                label = "L Cyl",
                placeholder = "-0.50",
                mode = FieldMode.SignedDecimal(scale = 2, forcePlus = false),
                onValueChange = { form = form.copy(leftCyl = it) },
                modifier = Modifier.weight(1f),
                onCommit = { onChange(form) }
            )
            OLTextField(
                value = form.leftAxis,
                label = "L Axis",
                placeholder = "175",
                mode = FieldMode.AxisDegrees,        // clamps 0..180 on commit
                onValueChange = { form = form.copy(leftAxis = it) },
                modifier = Modifier.weight(1f),
                onCommit = { onChange(form) }
            )
            OLTextField(
                value = form.leftAdd,
                label = "L Add",
                placeholder = "+1.75",
                mode = FieldMode.SignedDecimal(scale = 2, forcePlus = true),
                onValueChange = { form = form.copy(leftAdd = it) },
                modifier = Modifier.weight(1f),
                onCommit = { onChange(form) }
            )
        }

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
fun ContactLensForm(
    initialData: ProductFormData.LensData? = null,
    onChange: (ProductFormData.LensData) -> Unit,
) {
    var form by remember { mutableStateOf(initialData ?: ProductFormData.LensData()) }

    LaunchedEffect(Unit) {
        snapshotFlow { form }
            .debounce(200)
            .distinctUntilChanged()
            .collect { onChange(it) }
    }

    Column {
        FractionedSectionHeader("Product Information")
        OLTextField(
            value = form.productOwner,
            label = "Product Owner",
            mode = FieldMode.TitleCase(),
            modifier = Modifier.fillMaxWidth(),
            onValueChange = { form = form.copy(productOwner = it) },
            onCommit = { onChange(form) }
        )
        OLTextField(
            value = form.productDescription,
            label = "Description",
            mode = FieldMode.TitleCase(),
            modifier = Modifier.fillMaxWidth(),
            onValueChange = { form = form.copy(productDescription = it) },
            onCommit = { onChange(form) }
        )

        FractionedSectionHeader("Right Eye")
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OLTextField(
                value = form.rightSph,
                label = "R Sph",
                placeholder = "+1.50",
                mode = FieldMode.SignedDecimal(scale = 2, forcePlus = true),
                onValueChange = { form = form.copy(rightSph = it) },
                modifier = Modifier.weight(1f),
                onCommit = { onChange(form) } // still emits when blurred/done/next
            )
            OLTextField(
                value = form.rightCyl,
                label = "R Cyl",
                placeholder = "-0.50",
                mode = FieldMode.SignedDecimal(scale = 2, forcePlus = false),
                onValueChange = { form = form.copy(rightCyl = it) },
                modifier = Modifier.weight(1f),
                onCommit = { onChange(form) }
            )
            OLTextField(
                value = form.rightAxis,
                label = "R Axis",
                placeholder = "175",
                mode = FieldMode.AxisDegrees,        // clamps 0..180 on commit
                onValueChange = { form = form.copy(rightAxis = it) },
                modifier = Modifier.weight(1f),
                onCommit = { onChange(form) }
            )
            OLTextField(
                value = form.rightAdd,
                label = "R Add",
                placeholder = "+1.75",
                mode = FieldMode.SignedDecimal(scale = 2, forcePlus = true),
                onValueChange = { form = form.copy(rightAdd = it) },
                modifier = Modifier.weight(1f),
                onCommit = { onChange(form) }
            )
        }

        FractionedSectionHeader("Left Eye")

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OLTextField(
                value = form.leftSph,
                label = "L Sph",
                placeholder = "+1.50",
                mode = FieldMode.SignedDecimal(scale = 2, forcePlus = true),
                onValueChange = { form = form.copy(leftSph = it) },
                modifier = Modifier.weight(1f),
                onCommit = { onChange(form) } // still emits when blurred/done/next
            )
            OLTextField(
                value = form.leftCyl,
                label = "L Cyl",
                placeholder = "-0.50",
                mode = FieldMode.SignedDecimal(scale = 2, forcePlus = false),
                onValueChange = { form = form.copy(leftCyl = it) },
                modifier = Modifier.weight(1f),
                onCommit = { onChange(form) }
            )
            OLTextField(
                value = form.leftAxis,
                label = "L Axis",
                placeholder = "175",
                mode = FieldMode.AxisDegrees,        // clamps 0..180 on commit
                onValueChange = { form = form.copy(leftAxis = it) },
                modifier = Modifier.weight(1f),
                onCommit = { onChange(form) }
            )
            OLTextField(
                value = form.leftAdd,
                label = "L Add",
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
        FractionedSectionHeader("Product Information")
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OLTextField(
                value = form.productOwner,
                label = "Product Owner",
                mode = FieldMode.TitleCase(),
                modifier = Modifier.weight(1f),
                onValueChange = { form = form.copy(productOwner = it) },
                onCommit = { onChange(form) }
            )
            OLTextField(
                value = form.productType,
                label = "Product Type",
                mode = FieldMode.TitleCase(),
                modifier = Modifier.weight(1f),
                onValueChange = { form = form.copy(productType = it) },
                onCommit = { onChange(form) }
            )
        }
        OLTextField(
            value = form.productDescription,
            label = "Description",
            mode = FieldMode.TitleCase(),
            modifier = Modifier.fillMaxWidth(),
            onValueChange = { form = form.copy(productDescription = it) },
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

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun FrameFormPreview() {
    FrameForm(
        initialData = ProductFormData.FrameData(
            productOwner = "Ray-Ban",
            productDescription = "Aviator Classic",
            unitPrice = 5000,
            discountPct = 10,
            quantity = 1,
            total = 4500
        ),
        onChange = {}
    )
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun GlassFormPreview() {
    GlassForm(
        initialData = ProductFormData.GlassData(
            productOwner = "Essilor",
            productDescription = "Varilux Progressive",
            rightSph = "+1.50",
            rightCyl = "-0.50",
            rightAxis = "175",
            rightAdd = "+1.75",
            leftSph = "+1.25",
            leftCyl = "-0.75",
            leftAxis = "5",
            leftAdd = "+1.75",
            unitPrice = 8000,
            discountPct = 5,
            quantity = 1,
            total = 7600
        ),
        onChange = {}
    )
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun ContactLensFormPreview() {
    ContactLensForm(
        initialData = ProductFormData.LensData(
            productOwner = "Acuvue",
            productDescription = "Oasys Daily",
            rightSph = "-2.50",
            rightCyl = "-0.75",
            rightAxis = "180",
            rightAdd = "+1.00",
            leftSph = "-2.75",
            leftCyl = "-0.50",
            leftAxis = "10",
            leftAdd = "+1.00",
            unitPrice = 3000,
            discountPct = 15,
            quantity = 2,
            total = 5100
        ),
        onChange = {}
    )
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun GeneralProductFormPreview() {
    GeneralProductForm(
        initialData = ProductFormData.GeneralProductData(
            productOwner = "Zeiss",
            productType = "Cleaning Solution",
            productDescription = "Lens Care Kit",
            unitPrice = 500,
            discountPct = 0,
            quantity = 1,
            total = 500
        ),
        onChange = {}
    )
}

@Preview(showBackground = true, widthDp = 320, name = "Small Screen")
@Composable
private fun GlassFormSmallScreenPreview() {
    GlassForm(
        initialData = ProductFormData.GlassData(
            rightSph = "+1.50",
            rightCyl = "-0.50"
        ),
        onChange = {}
    )
}
