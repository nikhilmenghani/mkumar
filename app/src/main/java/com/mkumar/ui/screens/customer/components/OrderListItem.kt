package com.mkumar.ui.screens.customer.components

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mkumar.ui.screens.customer.model.OrderRowUi
import com.mkumar.viewmodel.OrderRowAction
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun OrderListItem(
    row: OrderRowUi,
    onAction: (OrderRowAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dateFmt = DateTimeFormatter.ofPattern("EEE, MMM d • h:mm a")
    val interaction = remember { MutableInteractionSource() }
    ElevatedCard(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(
                interactionSource = interaction,
                indication = ripple(),                 // nice ripple on tap
                onClick = { onAction(OrderRowAction.Open(row.id)) } // <-- fire Open
            )
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    dateFmt.format(row.occurredAt.atZone(ZoneId.systemDefault())),
                    style = MaterialTheme.typography.titleMedium
                )
                StatusBadges(row)
            }
            Spacer(Modifier.height(4.dp))
//            Text(
//                row.items.joinToString(", ") { it.name },
//                style = MaterialTheme.typography.bodyMedium,
//                maxLines = 2,
//                overflow = TextOverflow.Ellipsis
//            )
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total: ₹${row.amount}", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (row.hasInvoice) {
                        FilledTonalIconButton(onClick = { onAction(OrderRowAction.ViewInvoice(row.id, row.invoiceNumber)) }) {
                            Icon(Icons.Outlined.PictureAsPdf, contentDescription = "Invoice")
                        }
                    }
                    FilledTonalIconButton(onClick = { onAction(OrderRowAction.Share(row.id, row.invoiceNumber)) }) {
                        Icon(Icons.Outlined.Share, contentDescription = "Share")
                    }
                    IconButton(onClick = { onAction(OrderRowAction.Delete(row.id)) }) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Delete")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadges(row: OrderRowUi) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        if (row.isQueued) AssistChip(onClick = {}, label = { Text("Queued") })
        if (row.isSynced) AssistChip(onClick = {}, label = { Text("Synced") })
    }
}

@Composable
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
fun OrderListItemPreview() {
    OrderListItem(
        row = OrderRowUi(
            id = "1",
            occurredAt = java.time.Instant.now(),
            invoiceNumber = "500",
//            items = emptyList(),
            amount = 1250,
            hasInvoice = true,
            isQueued = true,
            isSynced = false,
            remainingBalance = 0
        ),
        onAction = {},
        modifier = Modifier.fillMaxWidth()
    )
}
