package com.mkumar.ui.screens.customer.components

import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.AssistChip
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
import com.mkumar.common.extension.DateFormat
import com.mkumar.common.extension.formatAsDate
import com.mkumar.common.extension.toLong
import com.mkumar.ui.components.ProMenuItem
import com.mkumar.ui.components.ProOverflowMenuIcons
import com.mkumar.viewmodel.OrderRowAction
import com.mkumar.viewmodel.OrderRowUi
import java.time.Instant

@Composable
fun OrderListItem(
    row: OrderRowUi,
    onAction: (OrderRowAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var menuOffsetPx by remember { mutableStateOf(Offset.Zero) }
    var anchorHeightPx by remember { mutableStateOf(0) }
    val interaction = remember { MutableInteractionSource() }
    val haptics = LocalHapticFeedback.current
    val density = LocalDensity.current
    val invoiceNumber = row.invoiceNumber
    val isPaid = row.remainingBalance == 0
    val containerColor = ledgerContainerColor(isPaid)
    val totalToShow = if ((row.adjustedTotal ?: 0) != 0) row.adjustedTotal!! else row.amount

    ElevatedCard(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, CardDefaults.elevatedShape),
        colors = CardDefaults.elevatedCardColors(containerColor = containerColor),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        ProOverflowMenuIcons(
            expanded = menuExpanded,
            onExpandedChange = { menuExpanded = it },
            menuOffset = with(density) { DpOffset(menuOffsetPx.x.toDp() + 8.dp, menuOffsetPx.y.toDp() + 8.dp) },
            items = buildList {
                add(
                    ProMenuItem(
                        title = "Invoice",
                        supportingText = "Generate or view invoice PDF",
                        icon = Icons.Outlined.PictureAsPdf,
                        onClick = { onAction(OrderRowAction.ViewInvoice(row.id, row.invoiceNumber)) }
                    )
                )
                add(
                    ProMenuItem(
                        title = "Share",
                        supportingText = "Share order details",
                        icon = Icons.Outlined.Share,
                        startNewGroup = true,
                        onClick = { onAction(OrderRowAction.Share(row.id, row.invoiceNumber)) }
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
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .clickable(interactionSource = interaction, role = Role.Button) {
                            if (menuExpanded) menuExpanded = false else onAction(OrderRowAction.Open(row.id))
                        }
                        .onGloballyPositioned { coords -> anchorHeightPx = coords.size.height }
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = {
                                    if (menuExpanded) menuExpanded = false else onAction(OrderRowAction.Open(row.id))
                                },
                                onLongPress = { offset ->
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    menuOffsetPx = Offset(offset.x, offset.y - anchorHeightPx)
                                    menuExpanded = true
                                }
                            )
                        }
                ) {
                    // Row 1: Invoice (left) + Total (right)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (invoiceNumber.isNotBlank()) {
                            Text(
                                text = "#$invoiceNumber",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else {
                            Spacer(Modifier.weight(1f))
                        }
                        LedgerRowCompact(
                            label = "Total",
                            value = "₹$totalToShow",
                            emphasize = (row.adjustedTotal ?: 0) != 0
                        )
                    }

                    Spacer(Modifier.height(6.dp))

                    // Row 2: Last Updated (left) + Remaining (right if > 0)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TimeBadges(row)
                        if (row.remainingBalance > 0) {
                            LedgerRowCompact(
                                label = "Remaining",
                                value = "₹${row.remainingBalance}",
                                valueColor = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun LedgerRowCompact(
    label: String,
    value: String,
    emphasize: Boolean = false,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.size(12.dp))
        Text(
            text = value,
            style = if (emphasize) {
                MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            } else {
                MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium)
            },
            color = valueColor
        )
    }
}

@Composable
private fun ledgerContainerColor(isPaid: Boolean): Color {
    val surface = MaterialTheme.colorScheme.surface
    val tint = if (isPaid) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.errorContainer
    val alpha = if (isPaid) 0.26f else 0.28f
    return tint.copy(alpha = alpha).compositeOver(surface)
}

@Composable
private fun TimeBadges(row: OrderRowUi) {
    AssistChip(
        onClick = {},
        label = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Last Updated:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = row.lastUpdatedAt.formatAsDate(DateFormat.SHORT_DATE_TIME),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun OrderListItemPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OrderListItem(
            row = OrderRowUi(
                id = "1",
                occurredAt = Instant.now().toLong(),
                invoiceNumber = "2",
                amount = 1250,
                remainingBalance = 0,
                lastUpdatedAt = Instant.now().toLong(),
                adjustedTotal = 1200
            ),
            onAction = {}
        )
        OrderListItem(
            row = OrderRowUi(
                id = "2",
                occurredAt = Instant.now().toLong(),
                invoiceNumber = "25",
                amount = 1250,
                remainingBalance = 300,
                lastUpdatedAt = Instant.now().toLong(),
                adjustedTotal = 1200
            ),
            onAction = {}
        )
    }
}
