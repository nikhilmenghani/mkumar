package com.mkumar.ui.screens.customer.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mkumar.data.ProductFormData
import com.mkumar.model.ProductType
import com.mkumar.model.UiOrderItem
import com.mkumar.ui.components.cards.OrderAccordionItem

/**
 * A DoorDash-style "Products" section card.
 *
 * - Card container with rounded corners
 * - Header with order summary (placeholder for now)
 * - Full-width divider under header
 * - Each product row uses your OrderAccordionItem
 * - Short inset dividers (~92% width) between products
 */
@Composable
fun ProductsSectionCard(
    products: List<UiOrderItem>,
    productOwner: String,
    onFormSave: (String, ProductFormData) -> Unit,
    onDelete: (String) -> Unit,
    getTypeForProduct: (UiOrderItem) -> ProductType,
    initiallyExpandedId: String? = null,
    modifier: Modifier = Modifier
)
 {
    if (products.isEmpty()) return

    Card(
        modifier = modifier.fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // ---------------------------------------------------------
            // HEADER (placeholder — replace later with real summary)
            // ---------------------------------------------------------
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(
                    text = "Products",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${products.size} items in this order",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            HorizontalDivider()

            // ---------------------------------------------------------
            // PRODUCT LIST
            // ---------------------------------------------------------
            products.forEachIndexed { index, product ->

                // Product row
                OrderAccordionItem(
                    productOwner = productOwner,
                    selectedProduct = product,
                    selectedType = getTypeForProduct(product),
                    onFormSave = onFormSave,
                    onDelete = onDelete,
                    grouped = true,
                    initiallyExpanded = (product.id == initiallyExpandedId)
                )

                // Short divider between items (but NOT after last)
                if (index < products.lastIndex) {
                    ShortInsetDivider()
                }
            }
        }
    }
}

/**
 * A short horizontal divider (~92% width), centered.
 * Matches premium app design patterns (DoorDash, Uber, Instacart).
 */
@Composable
fun ShortInsetDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 4.dp),
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = 1.dp
        )
    }
}
