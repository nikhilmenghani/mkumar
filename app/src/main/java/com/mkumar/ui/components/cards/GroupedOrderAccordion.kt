package com.mkumar.ui.components.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mkumar.data.ProductFormData
import com.mkumar.ui.components.forms.defaultFormFor
import com.mkumar.ui.theme.AppColors
import com.mkumar.viewmodel.ProductType
import com.mkumar.viewmodel.UiOrderItem

@Composable
fun GroupedOrderAccordion(
    productOwner: String,
    items: List<UiOrderItem>,
    selectedTypeResolver: (UiOrderItem) -> ProductType?, // or pass a constant if list is homogeneous
    onFormSave: (String, ProductFormData) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier,
    outerShape: RoundedCornerShape = RoundedCornerShape(20.dp), // big group radius like your screenshot
    dividerThickness: Dp = 1.dp
) {
    if (items.isEmpty()) return

    // One elevated surface for the entire group
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = outerShape,
        colors = AppColors.elevatedCardColors(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            items.forEachIndexed { index, item ->
                val isFirst = index == 0
                val isLast = index == items.lastIndex
                val radius = 20.dp
                // Row-specific outer shape: only first/last have rounded corners
                val rowShape = when {
                    isFirst && isLast -> RoundedCornerShape(radius) // single item
                    isFirst          -> RoundedCornerShape(topStart = radius, topEnd = radius, bottomStart = 0.dp, bottomEnd = 0.dp)
                    isLast           -> RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = radius, bottomEnd = radius)
                    else             -> RoundedCornerShape(0.dp)
                }

                // Row
                OrderAccordionItem(
                    productOwner = productOwner,
                    initiallyExpanded = false,
                    selectedProduct = item,
                    selectedType = selectedTypeResolver(item),
                    onFormSave = onFormSave,
                    onDelete = onDelete,
                    grouped = true,
                    rowShape = rowShape
                )

                // Divider between rows (not after the last)
                if (!isLast) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .requiredHeight(dividerThickness)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewGroupedOrderAccordion() {
    val items = listOf(
        UiOrderItem(id = "1", productType = ProductType.Glass, productDescription = "Single Vision Glass", formData = defaultFormFor(ProductType.Glass, "Nikhil"), finalTotal = 1200, name = "Nikhil",
            quantity =
            1, unitPrice = 25, discountPercentage = 10),
        UiOrderItem(id = "2", productType = ProductType.Frame, productDescription = "Acetate Frame", formData = defaultFormFor(ProductType.Frame, "Nikhil"), finalTotal = 2200, name = "Nikhil",
            quantity = 1,
            unitPrice = 50, discountPercentage = 0),
        UiOrderItem(id = "3", productType = ProductType.Lens, productDescription = "Monthly Contacts", formData = defaultFormFor(ProductType.Lens, "Nikhil"), finalTotal = 1500, name =
            "Nikhil", quantity = 1, unitPrice = 35, discountPercentage = 5),
    )
    MaterialTheme {
        GroupedOrderAccordion(
            productOwner = "Nikhil",
            items = items,
            selectedTypeResolver = { it.productType },
            onFormSave = { _, _ -> },
            onDelete = {}
        )
    }
}
