package com.mkumar.ui.screens.customer.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.mkumar.common.extension.toUtcMillisForLocalDay
import com.mkumar.data.ProductFormData
import com.mkumar.model.ProductType
import com.mkumar.model.UiOrderItem
import com.mkumar.model.UiPaymentItem
import com.mkumar.ui.components.cards.OrderAccordionItem
import com.mkumar.ui.components.dialogs.ConfirmActionDialog
import com.mkumar.ui.components.inputs.FieldMode
import com.mkumar.ui.components.inputs.OLTextField
import com.mkumar.ui.components.pickers.MKDatePickerDialog
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
    var addPaymentOpen by remember { mutableStateOf(false) }
    var adjustOpen by remember { mutableStateOf(false) }
    var pendingDeleteId by remember { mutableStateOf<String?>(null) }

    val hasAdjusted = adjustedAmount != 0

    // ---------------------------------------------------------------
    // RESTORED ANIMATIONS (THIS WAS LOST EARLIER)
    // ---------------------------------------------------------------
    val animatedTotal by animateIntAsState(
        targetValue = if (hasAdjusted) adjustedAmount else totalAmount,
        label = ""
    )
    val animatedPaid by animateIntAsState(
        targetValue = paidTotal,
        label = ""
    )
    val animatedDue by animateIntAsState(
        targetValue = remainingBalance,
        label = ""
    )
    // ---------------------------------------------------------------

    // Sync adjust panel when expanding/collapsing
    LaunchedEffect(expanded) {
        if (adjustedAmount != 0 && !adjustOpen) adjustOpen = true
        if (!expanded) addPaymentOpen = false
    }

    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "chevron"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {

        Column {

            // -----------------------------------------------------------------
            //   HEADER NOW RECEIVES THE RESTORED ANIMATED NUMBERS
            // -----------------------------------------------------------------
            ProductsHeader(
                products = products,
                animatedTotal = animatedTotal,
                animatedPaid = animatedPaid,
                animatedDue = animatedDue,
                chevronRotation = rotation,
                onClick = { expanded = !expanded }
            )

            AnimatedVisibility(visible = expanded) {

                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    Text(
                        "Payment Entries",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
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
                                onDelete = {
                                    pendingDeleteId = it.id
                                }
                            )
                        }
                    }

                    AddPaymentRow(
                        isOpen = addPaymentOpen,
                        onToggle = { addPaymentOpen = !addPaymentOpen },
                        onAdd = onAddPayment
                    )

                    AdjustTotalRow(
                        isOpen = adjustOpen,
                        adjustedAmount = adjustedAmount,
                        onAdjustedChange = onAdjustedTotalChange,
                        onToggle = { adjustOpen = !adjustOpen }
                    )
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

            if (pendingDeleteId != null) {
                ConfirmActionDialog(
                    title = "Remove Payment?",
                    message = "This Payment will be removed from the entries. You can add it again later if needed.",
                    confirmLabel = "Delete",
                    dismissLabel = "Cancel",
                    icon = Icons.Outlined.DeleteForever,
                    highlightConfirmAsDestructive = true,
                    onConfirm = {
                        pendingDeleteId?.let { id -> onDeletePayment(id) }
                        pendingDeleteId = null
                    },
                    onDismiss = { pendingDeleteId = null }
                )
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
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AdjustTotalRow(
    isOpen: Boolean,
    adjustedAmount: Int,
    onAdjustedChange: (Int) -> Unit,
    onToggle: () -> Unit
) {
    var localValue by remember { mutableStateOf(adjustedAmount.toString()) }
    val rowHeight = 64.dp

    // When we open the row, sync local text with current adjustedAmount
    LaunchedEffect(isOpen) {
        if (isOpen) {
            localValue = adjustedAmount.toString()
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        AnimatedContent(
            targetState = isOpen,
            transitionSpec = {
                slideInHorizontally(
                    initialOffsetX = { full -> -full / 2 }
                ) + fadeIn() togetherWith
                        slideOutHorizontally(
                            targetOffsetX = { full -> full / 2 }
                        ) + fadeOut()
            }
        ) { open ->

            if (!open) {

                // COLLAPSED BUTTON (same pattern as Add Payment)
                FilledTonalButton(
                    onClick = { onToggle() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Outlined.Edit, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Adjust Total")
                }

            } else {

                // EXPANDED: HEADER + ONE LINE FIELD + CLOSE
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    // Header
                    Text(
                        text = "Adjusted Total",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(rowHeight),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        // Adjusted Total field
                        OLTextField(
                            value = localValue,
                            label = "Adjusted Total (₹)",
                            placeholder = "e.g. 1,200",
                            mode = FieldMode.Integer,
                            onValueChange = { txt ->
                                val filtered = txt.filter { it.isDigit() || it == ',' }
                                localValue = filtered.ifEmpty { "0" }
                                onAdjustedChange(localValue.toIntOrNull() ?: 0)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(rowHeight)
                        )

                        // Close button – reset + collapse
                        FilledTonalButton(
                            onClick = {
                                // Always reset back to 0 on close
                                localValue = "0"
                                onAdjustedChange(0)
                                onToggle()
                            },
                            modifier = Modifier
                                .height(rowHeight)
                                .widthIn(min = 92.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Icon(Icons.Outlined.Close, null)
                            Spacer(Modifier.width(6.dp))
                            Text("Close", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
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

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AddPaymentRow(
    isOpen: Boolean,
    onToggle: () -> Unit,
    onAdd: (amount: Int, atUtcMillis: Long) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now(ZoneId.systemDefault())) }
    var showPicker by remember { mutableStateOf(false) }

    val rowHeight = 44.dp

    val pattern = DateFormat.DEFAULT_DATE_ONLY.pattern
    val formatter = DateTimeFormatter.ofPattern(pattern)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        AnimatedContent(
            targetState = isOpen,
            transitionSpec = {
                slideInHorizontally(
                    initialOffsetX = { full -> -full / 2 }
                ) + fadeIn() togetherWith
                        slideOutHorizontally(
                            targetOffsetX = { full -> full / 2 }
                        ) + fadeOut()
            }
        ) { open ->

            if (!open) {

                FilledTonalButton(
                    onClick = onToggle,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Outlined.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add Payment")
                }

            } else {

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Text(
                        text = "Add Payment",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )

                    // LINE 1 — Amount
                    OLTextField(
                        value = amount,
                        label = "Amount",
                        mode = FieldMode.Integer,
                        onValueChange = { amount = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp)
                    )

                    // LINE 2 — Date + Save + Close
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        // Date chip
                        Surface(
                            modifier = Modifier
                                .weight(1.2f)
                                .height(rowHeight)
                                .clickable { showPicker = true },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Outlined.DateRange, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = date.format(formatter),
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1
                                )
                            }
                        }

                        // Save
                        FilledTonalButton(
                            onClick = {
                                if (amount.isNotBlank()) {
                                    val utcMillis = date.toUtcMillisForLocalDay()
                                    onAdd(amount.toInt(), utcMillis)

                                    amount = ""
                                    date = LocalDate.now(ZoneId.systemDefault())
                                    onToggle()
                                }
                            },
                            modifier = Modifier
                                .weight(0.9f)
                                .height(rowHeight),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Icon(Icons.Outlined.Add, null)
                            Spacer(Modifier.width(6.dp))
                            Text("Save", style = MaterialTheme.typography.labelSmall)
                        }

                        // Close
                        FilledTonalButton(
                            onClick = onToggle,
                            modifier = Modifier
                                .weight(0.9f)
                                .height(rowHeight),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Icon(Icons.Outlined.Delete, null)
                            Spacer(Modifier.width(6.dp))
                            Text("Close", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }

    if (showPicker) {
        MKDatePickerDialog(
            initialDate = date,
            onDismiss = { showPicker = false },
            onConfirm = { pickedDate ->
                date = pickedDate
                showPicker = false
            }
        )
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
