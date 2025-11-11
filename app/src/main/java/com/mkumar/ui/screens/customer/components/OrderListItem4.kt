package com.mkumar.ui.screens.customer.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.mkumar.ui.components.ProMenuItem
import com.mkumar.ui.components.ProOverflowMenu
import com.mkumar.ui.screens.customer.model.OrderRowUi
import com.mkumar.viewmodel.OrderRowAction
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun OrderListItem4(
    row: OrderRowUi,
    onAction: (OrderRowAction) -> Unit,
    modifier: Modifier = Modifier,

    // New optional inputs (non-breaking for your current model)
    adjustedTotal: Int? = null,        // show when != 0; else fall back to row.amount as Total
    invoiceNumber: String? = null,     // e.g., "INV-1024"
    updatedAt: Instant? = null,        // shown on the right of the header row
    productTypesCount: Int? = null     // "n × product types"
) {
    val createdFmt = remember { DateTimeFormatter.ofPattern("EEE, MMM d • h:mm a") }
    val updatedFmt = remember { DateTimeFormatter.ofPattern("MMM d, h:mm a") }

    var menuExpanded by remember { mutableStateOf(false) }
    var menuOffsetPx by remember { mutableStateOf(Offset.Zero) }
    var anchorHeightPx by remember { mutableStateOf(0) }
    val interaction = remember { MutableInteractionSource() }
    val haptics = LocalHapticFeedback.current
    val density = LocalDensity.current

    val isPaid = row.remainingBalance == 0
    val containerColor = orderLedgerContainerColor(isPaid)

    ElevatedCard(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = containerColor),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        ProOverflowMenu(
            expanded = menuExpanded,
            onExpandedChange = { menuExpanded = it },
            menuOffset = with(density) {
                DpOffset(
                    x = menuOffsetPx.x.toDp() + 8.dp,
                    y = menuOffsetPx.y.toDp() + 8.dp
                )
            },
            items = buildList {
                add(
                    ProMenuItem(
                        title = "Invoice",
                        supportingText = "Generate or view invoice PDF",
                        icon = Icons.Outlined.PictureAsPdf,
                        onClick = { onAction(OrderRowAction.ViewInvoice(row.id)) }
                    )
                )
                add(
                    ProMenuItem(
                        title = "Share",
                        supportingText = "Share order details",
                        icon = Icons.Outlined.Share,
                        startNewGroup = true,
                        onClick = { onAction(OrderRowAction.Share(row.id)) }
                    )
                )
                add(
                    ProMenuItem(
                        title = "Delete",
                        supportingText = "Remove this order",
                        icon = Icons.Outlined.Delete,
                        destructive = true,
                        startNewGroup = true,
                        onClick = { onAction(OrderRowAction.Delete(row.id)) }
                    )
                )
            },
            anchor = {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .clickable(
                            interactionSource = interaction,
                            role = Role.Button
                        ) {
                            if (menuExpanded) menuExpanded = false
                            else onAction(OrderRowAction.Open(row.id))
                        }
                        .onGloballyPositioned { coords -> anchorHeightPx = coords.size.height }
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = {
                                    if (menuExpanded) menuExpanded = false
                                    else onAction(OrderRowAction.Open(row.id))
                                },
                                onLongPress = { offset ->
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    menuOffsetPx = Offset(
                                        x = offset.x,
                                        y = offset.y - anchorHeightPx
                                    )
                                    menuExpanded = true
                                }
                            )
                        }
                ) {
                    // ── Header: Created (left) · Updated (right)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = createdFmt.format(row.occurredAt.atZone(ZoneId.systemDefault())),
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Invoice number (bigger font)
                    if (!invoiceNumber.isNullOrBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "#$invoiceNumber",
                            // bigger than default titleSmall
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Product types count line
                    val typesLine = productTypesCount?.let { "$it × product types" }
                        ?: row.itemsLabel.takeIf { it.isNotBlank() } // fallback to your existing label
                    if (!typesLine.isNullOrBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = typesLine,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    // ── Two-column ledger
                    LedgerRow(
                        label = if ((adjustedTotal ?: 0) != 0) "Total" else "Total",
                        // show base total (row.amount)
                        value = "₹${row.amount}"
                    )

                    if ((adjustedTotal ?: 0) != 0) {
                        LedgerRow(
                            label = "Adjusted Total",
                            value = "₹${adjustedTotal!!}",
                            emphasize = true // make adjusted stand out slightly
                        )
                    }

                    // Remaining: show ONLY when > 0, colored red
                    if (row.remainingBalance > 0) {
                        LedgerRow(
                            label = "Remaining",
                            value = "₹${row.remainingBalance}",
                            valueColor = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        )
    }
}

@Composable
private fun LedgerRow(
    label: String,
    value: String,
    emphasize: Boolean = false,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (emphasize) {
                MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium)
            } else {
                MaterialTheme.typography.bodyMedium
            },
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = value,
            style = if (emphasize) {
                MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            } else {
                MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium)
            },
            color = valueColor,
            maxLines = 1
        )
    }
}

@Composable
private fun orderLedgerContainerColor(isPaid: Boolean): Color {
    // Stronger tint so the card clearly reads "green for paid / red for due"
    val surface = MaterialTheme.colorScheme.surface
    val tint = if (isPaid) {
        // success-ish
        MaterialTheme.colorScheme.tertiaryContainer
    } else {
        // due
        MaterialTheme.colorScheme.errorContainer
    }
    // Make it opaque via pre-composite to avoid underlay blending inconsistencies
    val subtleTint = tint.copy(alpha = 0.28f)
    return subtleTint.compositeOver(surface)
}

@Preview(showBackground = true)
@Composable
fun OrderListItem4Preview() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OrderListItem4(
            row = OrderRowUi(
                id = "1",
                occurredAt = Instant.now(),
                itemsLabel = "",
                amount = 1250,
                hasInvoice = true,
                isQueued = true,
                isSynced = false,
                remainingBalance = 0 // ✅ green card
            ),
            invoiceNumber = "INV-1024",
            updatedAt = Instant.now(),
            productTypesCount = 3,
            onAction = {}
        )
        OrderListItem4(
            row = OrderRowUi(
                id = "2",
                occurredAt = Instant.now(),
                itemsLabel = "",
                amount = 950,
                hasInvoice = true,
                isQueued = false,
                isSynced = true,
                remainingBalance = 200 // 🔴 red card
            ),
            adjustedTotal = 1100,
            invoiceNumber = "INV-2042",
            updatedAt = Instant.now(),
            productTypesCount = 2,
            onAction = {}
        )
    }
}
