package com.mkumar.ui.screens.customer.components

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mkumar.data.ProductFormData
import com.mkumar.model.ProductType
import com.mkumar.model.UiOrderItem
import com.mkumar.ui.components.cards.OrderAccordionItem

@Composable
fun ProductsSectionCard(
    totalAmount: Int,
    adjustedAmount: Int,
    remainingBalance: Int,
    advanceTotal: Int,
    products: List<UiOrderItem>,
    productOwner: String,
    onFormSave: (String, ProductFormData) -> Unit,
    onDelete: (String) -> Unit,
    getTypeForProduct: (UiOrderItem) -> ProductType,
    initiallyExpandedId: String? = null,
    modifier: Modifier = Modifier
) {
    if (products.isEmpty()) return

    // Animate values smoothly
    val animatedTotal by animateIntAsState(targetValue = if (adjustedAmount != 0) adjustedAmount else totalAmount, label = "")
    val animatedPaid by animateIntAsState(targetValue = advanceTotal, label = "")
    val animatedDue by animateIntAsState(targetValue = remainingBalance, label = "")

    Card(
        modifier = modifier
            .fillMaxWidth()
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
            // HEADER WITH TOTAL ON FIRST ROW,
            // PAID + DUE ON SECOND ROW
            // ---------------------------------------------------------
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    // LEFT — Title + items count
                    Column {
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

                    // RIGHT — Total in row 1, Paid + Due in row 2
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {

                        // ROW 1 → TOTAL
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AmountBadge(
                                label = "Total",
                                amount = animatedTotal,
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        // ROW 2 → PAID + DUE side-by-side
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AmountBadge(
                                label = "Paid",
                                amount = animatedPaid,
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )

                            AmountBadge(
                                label = "Due",
                                amount = animatedDue,
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            HorizontalDivider()

            // ---------------------------------------------------------
            // PRODUCT LIST
            // ---------------------------------------------------------
            products.forEachIndexed { index, product ->

                OrderAccordionItem(
                    productOwner = productOwner,
                    selectedProduct = product,
                    selectedType = getTypeForProduct(product),
                    onFormSave = onFormSave,
                    onDelete = onDelete,
                    grouped = true,
                    initiallyExpanded = (product.id == initiallyExpandedId)
                )

                if (index < products.lastIndex) {
                    ShortInsetDivider()
                }
            }
        }
    }
}

@Composable
fun AmountBadge(
    label: String,
    amount: Int,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = containerColor,
        contentColor = contentColor,
        tonalElevation = 0.dp,
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.35f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "$label:",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = "₹$amount",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

/**
 * A short horizontal divider (~92% width), centered.
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
