package com.mkumar.ui.screens.customer.components

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mkumar.ui.screens.customer.model.CustomerHeaderUi
import java.text.NumberFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Currency
import java.util.Locale

@Composable
fun CustomerHeader2(
    header: CustomerHeaderUi,
    modifier: Modifier = Modifier,
) {
    val dateFmt = DateTimeFormatter.ofPattern("MMM d, yyyy")
    val joined = header.joinedAt?.let {
        java.time.Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
    }

    ElevatedCard(modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // LEFT: Identity + meta
            Column(
                modifier = Modifier.weight(2f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = header.name,
                    style = MaterialTheme.typography.headlineSmall, // Bigger than titleLarge
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = header.phoneFormatted,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Customer since: ${joined?.let(dateFmt::format) ?: "—"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Orders: ${header.totalOrders}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // RIGHT: Money focus
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Spent",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = header.totalSpent.asINR(),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Remaining",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = header.totalRemaining.asINR(),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }
    }
}

private fun Int.asINR(): String =
    NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
        currency = Currency.getInstance("INR")
        maximumFractionDigits = 0 // adjust if you store paise
    }.format(this)

/* ---------- Preview ---------- */

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, widthDp = 360)
@Composable
private fun CustomerHeaderSplitPreview() {
    CustomerHeader(
        header = CustomerHeaderUi(
            id = "123",
            name = "Mahendra Menghani",
            phoneFormatted = "+91 98765 43210",
            joinedAt = System.currentTimeMillis() - 86400000L * 365, // 1 year ago
            totalOrders = 42,
            totalSpent = 12_500,
            totalRemaining = 2_500
        )
    )
}
