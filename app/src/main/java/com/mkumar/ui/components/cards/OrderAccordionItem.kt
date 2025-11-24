package com.mkumar.ui.components.cards

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mkumar.data.ProductFormData
import com.mkumar.data.ProductFormDataSaver
import com.mkumar.model.ProductType
import com.mkumar.model.UiOrderItem
import com.mkumar.model.productTypeDisplayNames
import com.mkumar.ui.components.badges.HighlightedBadge
import com.mkumar.ui.components.badges.OutlinedBadge
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

    val typeLabel = if (selectedType?.name == "GeneralProduct") {
        (selectedProduct.formData as? ProductFormData.GeneralProductData)?.productType
            ?: productTypeDisplayNames[selectedType]
    } else productTypeDisplayNames[selectedType]

    val title = selectedProduct.formData?.productDescription
        ?.ifBlank { "New ${productTypeDisplayNames[selectedType] ?: ""}" }
        ?: "New ${productTypeDisplayNames[selectedType] ?: ""}"

    val owner = draft.productOwner.orEmpty()

    val discountPercent = selectedProduct.discountPercentage
    val hasDiscount = discountPercent > 0

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = rowShape,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = if (grouped) CardDefaults.cardElevation(0.dp) else CardDefaults.cardElevation(1.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            // -------------------------
            // ROW 1 — Title + Product Type
            // -------------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                typeLabel?.let { OutlinedBadge(text = it) }
            }

            // -------------------------
            // ROW 2 — Badges cluster
            // -------------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // Owner (only when different)
                if (owner.isNotBlank() && owner != productOwner) {
                    OutlinedBadge(text = owner)
                }

                // Discount + Unit logic
                if (hasDiscount) {
                    OutlinedBadge(text = "Unit: ₹${selectedProduct.unitPrice}")
                    HighlightedBadge(
                        text = "${discountPercent}% off",
                        backgroundColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                }

                // Always show Total, but highlighted
                HighlightedBadge(text = "₹${selectedProduct.finalTotal}")
            }
        }

        // ---------------------------------------------------------
        // EXPANDED FORM AREA
        // ---------------------------------------------------------
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                ProductFormItem(
                    selectedProduct = selectedProduct,
                    draft = draft,
                    onDraftChange = { draft = it },
                    onSave = { id, formData ->
                        onFormSave(id, formData)
                        expanded = false
                    },
                    onDelete = onDelete
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewOrderAccordionItem() {
    val sample = UiOrderItem(
        id = "1",
        productType = ProductType.GeneralProduct,
        productDescription = "Sample Lens with long descriptive text",
        formData = defaultFormFor(ProductType.GeneralProduct, "Mahendra"),
        finalTotal = 1200,
        name = "Nikhil",
        quantity = 1,
        unitPrice = 25,
        discountPercentage = 20
    )

    OrderAccordionItem(
        productOwner = "Nikhil",
        selectedProduct = sample,
        selectedType = ProductType.GeneralProduct,
        onFormSave = { _, _ -> },
        onDelete = {}
    )
}
