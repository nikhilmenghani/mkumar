package com.mkumar.ui.screens.customer.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mkumar.common.extension.DateFormat
import com.mkumar.common.extension.formatAsDateTime
import com.mkumar.common.extension.toInstant
import com.mkumar.data.ProductFormData
import com.mkumar.model.ProductType
import com.mkumar.model.UiOrderItem
import com.mkumar.model.UiPaymentItem
import com.mkumar.ui.components.cards.OrderAccordionItem
import com.mkumar.ui.components.inputs.FieldMode
import com.mkumar.ui.components.inputs.OLTextField
import java.time.Instant

// -----------------------------------------------------------------------------
// CONFIGURATION — you can tune these freely
// -----------------------------------------------------------------------------
private const val FIELD_RATIO = 0.55f     // Adjusted Total takes 55%
private const val CHIP_RATIO = 0.45f      // Chip takes 45%

private const val ANIM_DURATION = 300     // smooth animation

// -----------------------------------------------------------------------------
// MAIN SECTION CARD
// -----------------------------------------------------------------------------
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

    var expanded by remember { mutableStateOf(false) }
    var adjustToggle by remember { mutableStateOf(false) }

    val hasAdjusted = adjustedAmount != 0
    val showAdjustedField = hasAdjusted || adjustToggle

    // Reset toggle when closing with 0 adjusted
    LaunchedEffect(expanded) {
        if (!expanded && adjustedAmount == 0) adjustToggle = false
    }

    val rotation by animateFloatAsState(
        if (expanded) 180f else 0f, label = "chevron"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {

        Column {

            ProductsHeader(
                products = products,
                animatedTotal = if (hasAdjusted) adjustedAmount else totalAmount,
                animatedPaid = paidTotal,
                animatedDue = remainingBalance,
                chevronRotation = rotation,
                onClick = { expanded = !expanded }
            )

            AnimatedVisibility(visible = expanded) {

                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {

                    AdjustTotalRow(
                        showAdjustedField = showAdjustedField,
                        adjustedAmount = adjustedAmount,
                        onAdjustedTotalChange = onAdjustedTotalChange,
                        onToggleClick = { adjustToggle = !adjustToggle }
                    )

                    Text(
                        "Payment Entries",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )


                    if (payments.isEmpty()) {
                        Text(
                            "No payments yet.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        payments.forEach {
                            CompactPaymentRow(
                                amount = it.amountPaid,
                                date = it.paymentAt.toInstant(),
                                onDelete = { onDeletePayment(it.id) }
                            )
                        }
                    }

                    AddPaymentRow(onAddPayment)
                }
            }

            HorizontalDivider()

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
                if (index < products.lastIndex) ShortInsetDivider()
            }
        }
    }
}

// -----------------------------------------------------------------------------
// PRODUCTS HEADER
// -----------------------------------------------------------------------------
@Composable
fun ProductsHeader(
    products: List<UiOrderItem>,
    animatedTotal: Int,
    animatedPaid: Int,
    animatedDue: Int,
    chevronRotation: Float,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Column {
                Text(
                    "Products",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "${products.size} items in this order",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AmountBadge(
                        label = "Total",
                        amount = animatedTotal,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
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
}

// -----------------------------------------------------------------------------
// ADJUST TOTAL ROW — smooth animation, stable, no crashes
// -----------------------------------------------------------------------------
@Composable
fun AdjustTotalRow(
    showAdjustedField: Boolean,
    adjustedAmount: Int,
    onAdjustedTotalChange: (Int) -> Unit,
    onToggleClick: () -> Unit
) {
    val rowHeight = 64.dp  // your preferred height

    val animatedFieldWeight by animateFloatAsState(
        targetValue = if (showAdjustedField) FIELD_RATIO else 0f,
        animationSpec = tween(ANIM_DURATION),
        label = "fieldRatio"
    )

    val animatedButtonWeight by animateFloatAsState(
        targetValue = if (showAdjustedField) CHIP_RATIO else 1f,
        animationSpec = tween(ANIM_DURATION),
        label = "buttonRatio"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = rowHeight),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // Adjusted Total Field
        if (animatedFieldWeight > 0f) {
            Box(
                modifier = Modifier
                    .weight(animatedFieldWeight)
                    .padding(end = 8.dp)
                    .height(rowHeight),
                contentAlignment = Alignment.CenterStart
            ) {

                OLTextField(
                    value = adjustedAmount.toString(),
                    label = "Adjusted Total",
                    placeholder = "4500",
                    mode = FieldMode.Integer,
                    onValueChange = { onAdjustedTotalChange(it.toIntOrNull() ?: 0) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(rowHeight)
                )
            }
        }

        // New: FilledTonalButton instead of AssistChip
        FilledTonalButton(
            onClick = onToggleClick,
            modifier = Modifier
                .weight(animatedButtonWeight)
                .height(rowHeight),
            contentPadding = PaddingValues(horizontal = 12.dp),
        ) {
            Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Adjust Total")
        }
    }
}



// -----------------------------------------------------------------------------
// PAYMENT ROW (aligned, responsive)
// -----------------------------------------------------------------------------
@Composable
fun CompactPaymentRow(
    amount: Int,
    date: Instant,
    onDelete: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // AMOUNT COLUMN (1/3)
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "₹$amount",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1
                )
            }

            // DATE COLUMN (1/3)
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = date.formatAsDateTime(DateFormat.DEFAULT_DATE_ONLY),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        maxLines = 1
                    )
                }
            }

            // DELETE COLUMN (1/3)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onDelete),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    "Delete",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        // Row Separator
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}


// -----------------------------------------------------------------------------
// ADD PAYMENT ROW
// -----------------------------------------------------------------------------
private const val ADD_AMOUNT_WEIGHT = 0.8f
private const val ADD_DATE_WEIGHT = 1.4f
private const val ADD_SAVE_WEIGHT = 0.8f
private val ADD_ROW_MIN_HEIGHT = 44.dp

@Composable
fun AddPaymentRow(
    onAddPayment: (amount: Int, at: Long) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(Instant.now()) }
    var showPicker by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

        Text(
            "Add Payment",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // AMOUNT (1/3)
            OLTextField(
                value = amount,
                label = "Amount",
                mode = FieldMode.Integer,
                onValueChange = { amount = it },
                modifier = Modifier.weight(1f)
            )

            // DATE PICKER (1/3)
            FilledTonalButton(
                onClick = { showPicker = true },
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Icon(Icons.Outlined.DateRange, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    date.formatAsDateTime(DateFormat.DEFAULT_DATE_ONLY),
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1
                )
            }

            // SAVE (1/3)
            FilledTonalButton(
                enabled = amount.isNotBlank(),
                onClick = {
                    onAddPayment(amount.toInt(), date.toEpochMilli())
                    amount = ""
                    date = Instant.now()
                },
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Save", style = MaterialTheme.typography.labelSmall)
            }
        }
    }

    if (showPicker) {
        val picker = rememberDatePickerState(initialSelectedDateMillis = date.toEpochMilli())

        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    date = Instant.ofEpochMilli(picker.selectedDateMillis!!)
                    showPicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = picker)
        }
    }
}


// -----------------------------------------------------------------------------
// BADGE + DIVIDER
// -----------------------------------------------------------------------------
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
                "$label:",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                "₹$amount",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun ShortInsetDivider() {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        HorizontalDivider(
            Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 4.dp),
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = 1.dp
        )
    }
}
