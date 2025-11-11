package com.mkumar.ui.screens.customer.components

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
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
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun OrderListItem4(
    row: OrderRowUi,
    onAction: (OrderRowAction) -> Unit,
    modifier: Modifier = Modifier,
    expanded: Boolean? = null,
    onExpandedChange: ((Boolean) -> Unit)? = null,
) {
    val dateFmt = DateTimeFormatter.ofPattern("EEE, MMM d • h:mm a")

    var internalExpanded by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    var menuOffsetPx by remember { mutableStateOf(Offset.Zero) }
    var anchorHeightPx by remember { mutableStateOf(0) }
    val interaction = remember { MutableInteractionSource() }
    val haptics = LocalHapticFeedback.current
    val isExpanded = expanded ?: internalExpanded
    val setExpanded: (Boolean) -> Unit = onExpandedChange ?: { internalExpanded = it }

    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "chevronRotation"
    )

    val containerColor = orderContainerColor(row.remainingBalance == 0)
    val density = LocalDensity.current

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
                        .onGloballyPositioned { coords ->
                            anchorHeightPx = coords.size.height  // in px
                        }
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = {
                                    if (menuExpanded) {
                                        menuExpanded = false
                                    } else {
                                        onAction(OrderRowAction.Open(row.id))
                                    }
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = dateFmt.format(row.occurredAt.atZone(ZoneId.systemDefault())),
                                style = MaterialTheme.typography.titleSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(onClick = { setExpanded(!isExpanded) }) {
                            Icon(
                                imageVector = Icons.Outlined.ExpandMore,
                                contentDescription = if (isExpanded) "Collapse" else "Expand",
                                modifier = Modifier.rotate(rotation)
                            )
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Total", style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.size(6.dp))
                        Text(
                            text = "₹${row.amount}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (row.remainingBalance != 0) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Remaining: ₹${row.remainingBalance}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.error
                            )
                        )
                    }
                }
            }
        )
    }
}

@Composable
private fun orderContainerColor(isPaid: Boolean): Color {
    val surface = MaterialTheme.colorScheme.surface
    val tint = if (isPaid)
        MaterialTheme.colorScheme.tertiaryContainer   // “greenish”
    else
        MaterialTheme.colorScheme.errorContainer      // “reddish”

    // Use a *subtle* tint, then pre-composite over surface to make it opaque.
    val subtleTint = tint.copy(alpha = 0.25f)
    return subtleTint.compositeOver(surface)
}

@Preview(showBackground = true)
@Composable
fun OrderListItem4Preview() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OrderListItem4(
            row = OrderRowUi(
                id = "1",
                occurredAt = java.time.Instant.now(),
                itemsLabel = "",
                amount = 1250,
                hasInvoice = true,
                isQueued = true,
                isSynced = false,
                remainingBalance = 0 // ✅ green card
            ),
            onAction = {}
        )
        OrderListItem4(
            row = OrderRowUi(
                id = "2",
                occurredAt = java.time.Instant.now(),
                itemsLabel = "",
                amount = 950,
                hasInvoice = true,
                isQueued = false,
                isSynced = true,
                remainingBalance = 200 // 🔴 red card
            ),
            onAction = {}
        )
    }
}
