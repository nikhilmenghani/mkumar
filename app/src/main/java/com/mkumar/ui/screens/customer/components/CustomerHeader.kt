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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mkumar.common.extension.nowUtcMillis
import com.mkumar.model.CustomerHeaderUi
import com.mkumar.model.UiCustomer
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
        ElevatedCard(
            modifier = Modifier,
            shape = MaterialTheme.shapes.medium
        ) {
            Column(Modifier.padding(16.dp)) {
                header.customer?.let { Text(it.name, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                Spacer(Modifier.height(4.dp))
                header.customer?.let { Text(it.phone, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                Spacer(Modifier.height(8.dp))
                val joined = header.customer?.let {
                    val date = java.time.Instant.ofEpochMilli(it.createdAt)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    fmt.format(date)
                } ?: "—"
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Customer since: $joined", style = MaterialTheme.typography.bodySmall)
                    Text("Orders: ${header.totalOrders}", style = MaterialTheme.typography.bodySmall)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Spent", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "₹${header.totalSpent}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Remaining", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "₹${header.totalRemaining}",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (header.totalRemaining > 0) Color.Red else MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun CustomerHeaderPreview() {
    val customer = UiCustomer(
        id = "123",
        name = "Mahendra Menghani",
        phone = "+91 98765 43210",
        createdAt = nowUtcMillis() - 86400000L * 365, // 1 year ago
    )
    CustomerHeader(
        header = CustomerHeaderUi(
            customer = customer,
            totalOrders = 42,
            totalSpent = 12500,
            totalRemaining = 2500
        )
    )
}
