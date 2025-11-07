package com.mkumar.ui.screens.customer.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    Box(
        modifier = modifier
            .border(1.dp, Color.Gray, shape = MaterialTheme.shapes.medium)
            .padding(0.dp)
    ) {
        ElevatedCard(modifier = Modifier,
            shape = MaterialTheme.shapes.medium) {
            Column(Modifier.padding(16.dp)) {
                Text(header.name, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                Text(header.phoneFormatted, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                val joined = header.joinedAt?.let {
                    val date = java.time.Instant.ofEpochMilli(it)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    fmt.format(date)
                } ?: "—"
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Customer since: $joined", style = MaterialTheme.typography.bodySmall)
                    Text("Orders: ${header.totalOrders}", style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Spent: ₹${header.totalSpent}", style = MaterialTheme.typography.bodySmall)
                    Text("Remaining: ₹${header.totalRemaining}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun CustomerHeaderPreview() {
    CustomerHeader(
        header = CustomerHeaderUi(
            id = "123",
            name = "Mahendra Menghani",
            phoneFormatted = "+91 98765 43210",
            joinedAt = System.currentTimeMillis() - 86400000L * 365, // 1 year ago
            totalOrders = 42,
            totalSpent = 12500,
            totalRemaining = 2500
        )
    )
}
