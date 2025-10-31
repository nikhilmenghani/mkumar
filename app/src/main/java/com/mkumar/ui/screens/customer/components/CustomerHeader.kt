package com.mkumar.ui.screens.customer.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mkumar.ui.screens.customer.model.CustomerHeaderUi
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun CustomerHeader(
    header: CustomerHeaderUi,
    modifier: Modifier = Modifier,
) {
    val fmt = DateTimeFormatter.ofPattern("MMM d, yyyy")
    ElevatedCard(modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(header.name, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))
            Text(header.phoneFormatted, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                val joined = header.joinedAt?.let {
                    fmt.format(it.atZone(ZoneId.systemDefault()).toLocalDate())
                } ?: "—"
                Text("Customer since: $joined", style = MaterialTheme.typography.bodySmall)
                Text("Orders: ${header.totalOrders}", style = MaterialTheme.typography.bodySmall)
                Text("Spent: ₹${header.totalSpent}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}