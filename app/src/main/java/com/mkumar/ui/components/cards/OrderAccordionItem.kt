package com.mkumar.ui.components.cards

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mkumar.data.ProductFormData
import com.mkumar.data.ProductFormDataSaver
import com.mkumar.model.ProductType
import com.mkumar.model.UiOrderItem
import com.mkumar.model.productTypeDisplayNames
import com.mkumar.ui.components.forms.ProductFormItem
import com.mkumar.ui.components.forms.defaultFormFor
import com.mkumar.ui.theme.AppColors

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OrderAccordionItem(
    productOwner: String,
    initiallyExpanded: Boolean = false,
    selectedProduct: UiOrderItem?,
    selectedType: ProductType?,
    onFormSave: (String, ProductFormData) -> Unit,
    onDelete: (String) -> Unit,
    collapsedHeight: Dp = 92.dp,
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

    val selectedTypeText =
        if (selectedType?.name == "GeneralProduct") {
            (selectedProduct.formData as? ProductFormData.GeneralProductData)?.productType
                ?: productTypeDisplayNames[selectedType]
        } else {
            productTypeDisplayNames[selectedType]
        }

    val titleText = selectedProduct.formData?.productDescription
        ?.ifBlank { "New ${productTypeDisplayNames[selectedType] ?: ""}" }
        ?: "New ${productTypeDisplayNames[selectedType] ?: ""}"

    val owner: String = draft.productOwner.orEmpty()

    // ------------------------------------------------------------------
    // BORDER HANDLING — No double borders when grouped
    // ------------------------------------------------------------------
    val borderColor = MaterialTheme.colorScheme.outlineVariant

    val borderModifier = if (grouped) {
        Modifier.drawBehind {
            val strokeWidth = 1.dp.toPx()

            // Left
            drawRect(
                color = borderColor,
                topLeft = Offset(0f, 0f),
                size = Size(strokeWidth, size.height)
            )
            // Right
            drawRect(
                color = borderColor,
                topLeft = Offset(size.width - strokeWidth, 0f),
                size = Size(strokeWidth, size.height)
            )
            // Bottom
            drawRect(
                color = borderColor,
                topLeft = Offset(0f, size.height - strokeWidth),
                size = Size(size.width, strokeWidth)
            )
        }
    } else {
        Modifier.border(1.dp, borderColor, rowShape)
    }


    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .then(borderModifier),
        shape = rowShape,
        colors = cardColors,
        elevation = if (grouped) CardDefaults.cardElevation(0.dp) else CardDefaults.cardElevation(1.dp)
    ) {
        Column {

            // ------------------------------------------------------------------
            // HEADER WITH BACKGROUND WHEN COLLAPSED
            // ------------------------------------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (!expanded)
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        else
                            Color.Transparent
                    )
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // LEFT COLUMN — Description (elastic height + bigger font)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = titleText,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // RIGHT COLUMN — TWO ROWS
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Center
                ) {

                    // Row 1 — Amount + Arrow
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "₹${selectedProduct.finalTotal}",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )

                        Icon(
                            imageVector = if (expanded)
                                Icons.Outlined.KeyboardArrowUp
                            else Icons.Outlined.KeyboardArrowDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    // Row 2 — Badges (owner + type)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (owner.isNotBlank() && owner != productOwner) {
                            OutlinedBadge(text = owner)
                        }
                        selectedTypeText?.let {
                            OutlinedBadge(text = it)
                        }
                    }
                }
            }

            // ------------------------------------------------------------------
            // EXPANDED CONTENT (FORM)
            // ------------------------------------------------------------------
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
}

@Composable
private fun OutlinedBadge(text: String) {
    Surface(
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        color = Color.Transparent,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewOrderAccordionItem_Compact() {
    val sampleProduct = UiOrderItem(
        id = "1",
        productType = ProductType.GeneralProduct,
        productDescription = "Sample Lens for customer order",
        formData = defaultFormFor(ProductType.GeneralProduct, "Mahendra"),
        finalTotal = 1200,
        name = "Nikhil",
        quantity = 1,
        unitPrice = 25,
        discountPercentage = 10
    )

    OrderAccordionItem(
        productOwner = "Nikhil",
        selectedProduct = sampleProduct,
        selectedType = ProductType.GeneralProduct,
        onFormSave = { _, _ -> },
        onDelete = {}
    )
}
