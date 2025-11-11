package com.mkumar.ui.screens.customer.components

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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mkumar.ui.screens.customer.model.OrderRowUi
import com.mkumar.viewmodel.OrderRowAction
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun OrderListItem2(
    row: OrderRowUi,
    onAction: (OrderRowAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dateFmt = DateTimeFormatter.ofPattern("EEE, MMM d • h:mm a")
    val interaction = remember { MutableInteractionSource() }
    ElevatedCard(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    dateFmt.format(row.occurredAt.atZone(ZoneId.systemDefault())),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                StatusBadges(row)
            }
            Spacer(Modifier.height(8.dp))
//            Text(
//                row.items.joinToString(", ") { it.name },
//                style = MaterialTheme.typography.bodyLarge,
//                maxLines = 2,
//                overflow = TextOverflow.Ellipsis,
//                color = MaterialTheme.colorScheme.onSurface
//            )
            Spacer(Modifier.height(12.dp))
            androidx.compose.material3.HorizontalDivider()
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total: ₹${row.amount}", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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

@Composable
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
fun OrderListItem2Preview() {
    OrderListItem2(
        row = OrderRowUi(
            id = "1",
            occurredAt = java.time.Instant.now(),
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
