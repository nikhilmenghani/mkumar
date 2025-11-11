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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AssistChip
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
    productOwner: String,
    initiallyExpanded: Boolean = false,
    selectedProduct: UiOrderItem?,
    selectedType: ProductType?,
    onFormSave: (String, ProductFormData) -> Unit,
    onDelete: (String) -> Unit,
    collapsedHeight: Dp = 76.dp,
    grouped: Boolean = false,
    rowShape: RoundedCornerShape = RoundedCornerShape(12.dp),
) {
    if (selectedProduct == null) return

    var expanded by remember { mutableStateOf(initiallyExpanded) }
    val draftBeforeState = rememberSaveable(
        selectedProduct.id,
        saver = ProductFormDataSaver
    ) { selectedProduct.formData ?: defaultFormFor(selectedProduct.productType, productOwner) }
    var draft by remember { mutableStateOf(draftBeforeState) }

    val cardColors = AppColors.elevatedCardColors()

    // Title fallback text
    val titleText = selectedProduct.formData?.productDescription
        ?.ifBlank { "New ${selectedType ?: ""}" }
        ?: "New ${selectedType ?: ""}"

    // Optional owner (subtitle)
    val owner = draft.productOwner

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* expand/collapse handled by header click below */ },
        shape = rowShape,
        colors = cardColors,
        elevation = if (grouped) CardDefaults.cardElevation(0.dp) else CardDefaults.cardElevation(1.dp)
    ) {
        Column {
            // Header (collapsible area)
            var shrinkTitle by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .heightIn(min = collapsedHeight) // lets header grow if title + subtitle need space
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.Top
            ) {
                // Left: Title + Owner chip
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp)
                ) {
                    Text(
                        text = titleText,
                        style = (if (shrinkTitle) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge)
                            .copy(fontWeight = FontWeight.Medium),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        onTextLayout = { layoutResult ->
                            // If it still overflows on two lines, shrink once
                            if (layoutResult.hasVisualOverflow && !shrinkTitle) {
                                shrinkTitle = true
                            }
                        }
                    )

                    if (owner.isNotBlank()) {
                        Spacer(modifier = Modifier.padding(top = 4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AssistChip(
                                onClick = { /* no-op; header handles main click */ },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Person,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(16.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        owner,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            )
//                             If you also want to show product type alongside owner as a chip:
                            if (selectedType != null) {
                                Spacer(Modifier.width(6.dp))
                                AssistChip(
                                    onClick = { },
                                    label = { Text(selectedType.name, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }
                }

                // Right: Price + Chevron
                Column (
                    modifier = Modifier.width(84.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "₹${selectedProduct.finalTotal}",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        modifier = Modifier.width(84.dp),
                        textAlign = TextAlign.End
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                        contentDescription = if (expanded) "Collapse item" else "Expand item",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .align(Alignment.End)
                            .size(32.dp)
                    )
                }
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

    // Subtle outline when not grouped and collapsed
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
        formData = defaultFormFor(ProductType.Lens, "Nikhil"),
        finalTotal = 1200,
        name = "Nikhil",
        quantity = 1,
        unitPrice = 25,
        discountPercentage = 10
    )
    OrderAccordionItem(
        productOwner = "Nikhil",
        selectedProduct = sampleProduct,
        selectedType = ProductType.Lens,
        onFormSave = { _, _ -> },
        onDelete = {},
        collapsedHeight = 76.dp
    )
}
