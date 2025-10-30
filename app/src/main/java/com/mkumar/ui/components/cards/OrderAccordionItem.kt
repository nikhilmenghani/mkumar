package com.mkumar.ui.components.cards

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mkumar.data.ProductEntry
import com.mkumar.data.ProductFormData
import com.mkumar.data.ProductType
import com.mkumar.ui.components.forms.ProductFormItem

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OrderAccordionItem(
    selectedProduct: ProductEntry?,
    selectedType: ProductType?,
    getProductFormData: (ProductEntry) -> ProductFormData?,
    updateProductFormData: (String, ProductFormData) -> Unit,
    hasUnsavedChanges: (ProductEntry, ProductFormData?) -> Boolean,
    onFormSave: (String, ProductFormData) -> Unit,
    collapsedHeight: Dp = 76.dp
) {
    if (selectedProduct == null) return
    var expanded by remember { mutableStateOf(false) }
    val shape = MaterialTheme.shapes.extraLarge // typically 24.dp
    val productFormData by rememberUpdatedState(getProductFormData(selectedProduct))
    val isDirty by remember(selectedProduct.id, productFormData) {
        derivedStateOf {
            !selectedProduct.isSaved || hasUnsavedChanges(selectedProduct, productFormData)
        }
    }

    Box(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    expanded = !expanded
                },
            shape = shape,
            colors = CardDefaults.elevatedCardColors(
                containerColor = if (expanded)
                    MaterialTheme.colorScheme.surfaceContainerHighest
                else
                    MaterialTheme.colorScheme.surfaceContainerLow
            ),
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = if (expanded) 6.dp else 4.dp,
                pressedElevation = 8.dp
            )
        ) {
            Column {
                // Collapsed header — fixed height
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .requiredHeight(collapsedHeight)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedProduct.productDescription.ifBlank { "New ${selectedType?.label}" },
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
                        text = "₹${selectedProduct?.finalTotal}",
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

                // Expanded form (column form)
                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ProductFormItem(
                            selectedProduct = selectedProduct,
                            productFormData = productFormData,
                            updateProductFormData = updateProductFormData,
                        )
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedButton(onClick = { expanded = false }) { Text("Delete") }
                            Button(onClick = {
                                expanded = false
                                productFormData?.let { onFormSave(selectedProduct.id, it) }
                            }) { Text("Save Item") }
                        }
                    }
                }
            }
        }

        // Subtle outline when collapsed to ensure it reads as a separate surface
        if (!expanded) {
            Box(
                Modifier
                    .matchParentSize()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
            )
        }
    }
}