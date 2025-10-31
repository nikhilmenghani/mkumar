package com.mkumar.ui.screens.customer.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mkumar.ui.screens.customer.model.OrderRowAction
import com.mkumar.ui.screens.customer.model.OrderRowUi
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun OrderListItem(
    row: OrderRowUi,
    onAction: (OrderRowAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dateFmt = DateTimeFormatter.ofPattern("EEE, MMM d • h:mm a")
    ElevatedCard(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)
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
            Text(
                row.itemsLabel,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total: ₹${row.amount}", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (row.hasInvoice) {
                        FilledTonalIconButton(onClick = { onAction(OrderRowAction.ViewInvoice(row.id)) }) {
                            Icon(Icons.Outlined.PictureAsPdf, contentDescription = "Invoice")
                        }
                    }
                    FilledTonalIconButton(onClick = { onAction(OrderRowAction.Share(row.id)) }) {
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