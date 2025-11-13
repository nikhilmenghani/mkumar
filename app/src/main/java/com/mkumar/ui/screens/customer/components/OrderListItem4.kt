package com.mkumar.ui.screens.customer.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OrderListItem4(
    row: OrderRowUi,
    onAction: (OrderRowAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val createdFmtTimeFirst = remember { DateTimeFormatter.ofPattern("MMM d, h:mm a") }

    var menuExpanded by remember { mutableStateOf(false) }
    var menuOffsetPx by remember { mutableStateOf(Offset.Zero) }
    var anchorHeightPx by remember { mutableStateOf(0) }
    val interaction = remember { MutableInteractionSource() }
    val haptics = LocalHapticFeedback.current
    val density = LocalDensity.current
    val invoiceNumber = row.invoiceNumber//.padStart(5, '0')

    val isPaid = row.remainingBalance == 0
    val containerColor = ledgerContainerColor(isPaid)

    val totalToShow = if ((row.adjustedTotal ?: 0) != 0) row.adjustedTotal!! else row.amount

    ElevatedCard(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outline, shape = CardDefaults.elevatedShape),
        colors = CardDefaults.elevatedCardColors(containerColor = containerColor),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        ProOverflowMenu(
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
                        .padding(horizontal = 16.dp, vertical = 12.dp)
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
                                    menuOffsetPx = Offset(x = offset.x, y = offset.y - anchorHeightPx)
                                    menuExpanded = true
                                }
                            )
                        }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            if (invoiceNumber.isNotBlank()) {
                                Text(
                                    text = "#$invoiceNumber",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = createdFmtTimeFirst.format(row.occurredAt.atZone(ZoneId.systemDefault())),
                                style = MaterialTheme.typography.titleSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Column(
                            modifier = Modifier.wrapContentWidth(Alignment.End),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            LedgerRowCompact(label = "Total", value = "₹$totalToShow", emphasize = (row.adjustedTotal ?: 0) != 0)
                            if (row.remainingBalance > 0) {
                                LedgerRowCompact(
                                    label = "Remaining",
                                    value = "₹${row.remainingBalance}",
                                    valueColor = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))
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
    val tint = if (isPaid) {
        MaterialTheme.colorScheme.tertiaryContainer // greenish
    } else {
        MaterialTheme.colorScheme.errorContainer    // reddish
    }
    // Strong enough to clearly read status, but not screaming
    val alpha = if (isPaid) 0.26f else 0.28f
    return tint.copy(alpha = alpha).compositeOver(surface)
}

@Preview(showBackground = true)
@Composable
private fun OrderListItem4Preview() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OrderListItem4(
            row = OrderRowUi(
                id = "1",
                occurredAt = Instant.now(),
                invoiceNumber = "390",
//                items = emptyList(),
                amount = 1250,
                hasInvoice = true,
                isQueued = true,
                isSynced = false,
                remainingBalance = 0 // ✅ green card
            ),
//            productTypeCounts = listOf("Lens" to 2, "Frame" to 1),
            onAction = {}
        )
        OrderListItem4(
            row = OrderRowUi(
                id = "2",
                occurredAt = Instant.now(),
                invoiceNumber = "390",
//                items = emptyList(),
                amount = 950,
                hasInvoice = true,
                isQueued = false,
                isSynced = true,
                remainingBalance = 200 // 🔴 red card
            ),
//            productTypeCounts = listOf("Lens" to 1, "Frame" to 1, "Accessories" to 2),
            onAction = {}
        )
    }
}
