package com.mkumar.ui.screens.customer.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mkumar.ui.screens.customer.model.OrderRowUi
import com.mkumar.viewmodel.OrderRowAction
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun OrderListItem3(
    row: OrderRowUi,
    onAction: (OrderRowAction) -> Unit,
    modifier: Modifier = Modifier,
    // Optional controlled expansion
    expanded: Boolean? = null,
    onExpandedChange: ((Boolean) -> Unit)? = null,
) {
    val dateFmt = DateTimeFormatter.ofPattern("EEE, MMM d • h:mm a")
    val interaction = remember { MutableInteractionSource() }

    // Uncontrolled fallback if not provided
    var internalExpanded by remember { mutableStateOf(false) }
    val isExpanded = expanded ?: internalExpanded
    val setExpanded: (Boolean) -> Unit = onExpandedChange ?: { internalExpanded = it }

    val rotation by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "chevRotation")

    ElevatedCard(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .clickable(
                    interactionSource = interaction,
                    indication = ripple(),
                    onClick = { setExpanded(!isExpanded) }
                )
                .padding(16.dp)
        ) {
            // Header row: Date + status + chevron
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = dateFmt.format(row.occurredAt.atZone(ZoneId.systemDefault())),
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(6.dp))
                    StatusBadges3(row)
                }
                Icon(
                    imageVector = Icons.Outlined.ExpandMore,
                    contentDescription = "Expand",
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(rotation)
                )
            }

            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total: ₹${row.amount}",
                    style = MaterialTheme.typography.titleSmall
                )
                // Quick actions are revealed when expanded; keep Share visible for convenience if you like
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (row.hasInvoice) {
                            FilledTonalIconButton(onClick = { onAction(OrderRowAction.ViewInvoice(row.id, row.invoiceNumber)) }) {
                                Icon(Icons.Outlined.PictureAsPdf, contentDescription = "Invoice")
                            }
                            Spacer(Modifier.size(8.dp))
                        }
                        FilledTonalIconButton(onClick = { onAction(OrderRowAction.Share(row.id, row.invoiceNumber)) }) {
                            Icon(Icons.Outlined.Share, contentDescription = "Share")
                        }
                        Spacer(Modifier.size(8.dp))
                        IconButton(onClick = { onAction(OrderRowAction.Delete(row.id)) }) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Delete")
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
private fun StatusBadges3(row: OrderRowUi) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        if (row.isQueued) AssistChip(onClick = {}, label = { Text("Queued") })
        if (row.isSynced) AssistChip(onClick = {}, label = { Text("Synced") })
    }
}

@Preview(showBackground = true)
@Composable
fun OrderListItem3Preview() {
    OrderListItem3(
        row = OrderRowUi(
            id = "1",
            occurredAt = java.time.Instant.now(),
            invoiceNumber = "390",
//            items = emptyList(),
            amount = 1250,
            hasInvoice = true,
            isQueued = true,
            isSynced = false,
            remainingBalance = 0
        ),
        onAction = {}
    )
}
