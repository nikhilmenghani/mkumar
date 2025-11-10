package com.mkumar.ui.components.cards

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mkumar.data.ProductFormData
import com.mkumar.data.ProductFormDataSaver
import com.mkumar.ui.components.forms.ProductFormItem
import com.mkumar.ui.components.forms.defaultFormFor
import com.mkumar.ui.theme.AppColors
import com.mkumar.viewmodel.ProductType
import com.mkumar.viewmodel.UiOrderItem

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OrderAccordionItem(
    initiallyExpanded: Boolean = false,
    selectedProduct: UiOrderItem?,
    selectedType: ProductType?,
    onFormSave: (String, ProductFormData) -> Unit,
    onDelete: (String) -> Unit,
    collapsedHeight: Dp = 76.dp,
    // NEW: when true, this row is rendered flat (no card drop shadow/outline)
    grouped: Boolean = false,
    // NEW: override the outer shape so first/last rows can be rounded appropriately
    rowShape: RoundedCornerShape = RoundedCornerShape(12.dp),
) {
    if (selectedProduct == null) return

    var expanded by remember { mutableStateOf(initiallyExpanded) }
    val draftBeforeState = rememberSaveable(
        selectedProduct.id,
        saver = ProductFormDataSaver
    ) { selectedProduct.formData ?: defaultFormFor(selectedProduct.productType) }
    var draft by remember { mutableStateOf(draftBeforeState) }

    val cardColors = AppColors.elevatedCardColors()

    // Use Card only for easy ripple/pressed behavior; elevation 0 when grouped
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* expand/collapse handled by header click below */ },
        shape = rowShape,
        colors = cardColors,
        elevation = if (grouped) CardDefaults.cardElevation(0.dp) else CardDefaults.cardElevation(1.dp)
    ) {
        Column {
            // Collapsed header — fixed height
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .requiredHeight(collapsedHeight)
                    .padding(horizontal = 16.dp)
                    .clickable { expanded = !expanded }, // header controls expansion
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedProduct.productDescription.ifBlank { "New ${selectedType?.toString()}" },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp)
                )
                Spacer(Modifier.width(16.dp))
                Text(
                    text = "₹${selectedProduct.finalTotal}",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    modifier = Modifier.width(84.dp),
                    textAlign = TextAlign.End
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = if (expanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Expanded form
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    ProductFormItem(
                        selectedProduct = selectedProduct,
                        draft = draft,
                        onDraftChange = { draft = it },
                        onSave = { productId, formData ->
                            onFormSave(productId, formData)
                            expanded = false
                        },
                        onDelete = onDelete
                    )
                }
            }
        }
    }

    if (!grouped && !expanded) {
        Box(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, rowShape)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewOrderAccordionItem() {
    val sampleProduct = UiOrderItem(
        id = "1",
        productType = ProductType.Lens,
        productDescription = "Sample Lens",
        formData = defaultFormFor(ProductType.Lens),
        finalTotal = 1200,
        name = "Nikhil",
        quantity = 1,
        unitPrice = 25,
        discountPercentage = 10
    )
    OrderAccordionItem(
        selectedProduct = sampleProduct,
        selectedType = ProductType.Lens,
        onFormSave = { _, _ -> },
        onDelete = {},
        collapsedHeight = 76.dp
    )
}
