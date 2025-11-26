package com.mkumar.ui.screens.customer.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mkumar.common.extension.toInstant
import com.mkumar.common.extension.toLong
import com.mkumar.data.ProductFormData
import com.mkumar.model.ProductType
import com.mkumar.model.UiOrderItem
import com.mkumar.model.UiPaymentItem
import com.mkumar.ui.components.cards.OrderAccordionItem
import com.mkumar.ui.components.inputs.FieldMode
import com.mkumar.ui.components.inputs.OLTextField
import com.mkumar.ui.components.items.PaymentListItem
import java.time.Instant

@Composable
fun ProductsSectionCard(
    totalAmount: Int,
    adjustedAmount: Int,
    remainingBalance: Int,
    paidTotal: Int,
    products: List<UiOrderItem>,
    productOwner: String,
    onFormSave: (String, ProductFormData) -> Unit,
    onDelete: (String) -> Unit,
    getTypeForProduct: (UiOrderItem) -> ProductType,
    onAddPayment: (amount: Int, paymentAt: Long) -> Unit,
    onDeletePayment: (paymentId: String) -> Unit,
    payments: List<UiPaymentItem>,
    onAdjustedTotalChange: (Int) -> Unit,
    initiallyExpandedId: String? = null,
    modifier: Modifier = Modifier
) {
    if (products.isEmpty()) return

    var expanded by remember {
        mutableStateOf(adjustedAmount != 0 && adjustedAmount != totalAmount)
    }

    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "productsChevron"
    )

    val animatedTotal by animateIntAsState(
        targetValue = if (adjustedAmount != 0) adjustedAmount else totalAmount,
        label = ""
    )
    val animatedPaid by animateIntAsState(targetValue = paidTotal, label = "")
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
            // EXISTING PRODUCTS HEADER (unchanged visually)
            // ---------------------------------------------------------
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Column {
                        Text(
                            text = "Products",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${products.size} items in this order",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {

                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // ROW 1 → TOTAL
                            AmountBadge(
                                label = "Total",
                                amount = animatedTotal,
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                            // ROW 2 → Paid + Due
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

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

                        Icon(
                            imageVector = Icons.Outlined.ExpandMore,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(20.dp)
                                .graphicsLayer { rotationZ = chevronRotation }
                        )
                    }
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // ---------------------------------------------------------
                    // Adjust Total Chip
                    // ---------------------------------------------------------
                    var showAdjusted by remember {
                        mutableStateOf(adjustedAmount != totalAmount)
                    }

                    AssistChip(
                        onClick = { showAdjusted = !showAdjusted },
                        label = { Text("Adjust Total") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )

                    if (showAdjusted) {
                        OLTextField(
                            value = adjustedAmount.toString(),
                            label = "Adjusted Total",
                            placeholder = "4500",
                            mode = FieldMode.Integer,
                            onValueChange = {
                                onAdjustedTotalChange(it.toIntOrNull() ?: 0)
                            }
                        )
                    }

                    // ---------------------------------------------------------
                    // Payment Entries Section
                    // ---------------------------------------------------------
                    Text(
                        text = "Payment Entries",
                        style = MaterialTheme.typography.titleSmall
                    )

                    if (payments.isEmpty()) {
                        Text(
                            text = "No payments yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        payments.forEach { p ->
                            PaymentListItem(
                                amount = p.amountPaid,
                                date = p.paymentAt.toInstant(),
                                onDelete = { onDeletePayment(p.id) }
                            )
                        }
                    }

                    // ---------------------------------------------------------
                    // Add Payment Row
                    // ---------------------------------------------------------
                    AddPaymentRow(
                        onAddPayment = onAddPayment
                    )
                }
            }

            HorizontalDivider()

            // ---------------------------------------------------------
            // PRODUCT LIST (unchanged)
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
fun AddPaymentRow(
    onAddPayment: (amount: Int, paymentAt: Long) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var date: Instant? by remember { mutableStateOf(null) }
    var showPicker by remember { mutableStateOf(false) }

    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {

        OLTextField(
            value = amount,
            label = "Amount",
            mode = FieldMode.Integer,
            modifier = Modifier.weight(1f),
            onValueChange = { amount = it }
        )

        FilledTonalIconButton(
            onClick = { showPicker = true }
        ) {
            Icon(Icons.Outlined.DateRange, contentDescription = "Select date")
        }

        FilledTonalButton(
            onClick = {
                onAddPayment(amount.toInt(), date!!.toLong())
                amount = ""
                date = null
            },
            enabled = amount.isNotBlank() && date != null
        ) {
            Icon(Icons.Outlined.Add, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text("Save")
        }
    }

    if (showPicker) {
        val pickerState = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = pickerState.selectedDateMillis!!
                    date = Instant.ofEpochMilli(millis)
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = pickerState)
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
