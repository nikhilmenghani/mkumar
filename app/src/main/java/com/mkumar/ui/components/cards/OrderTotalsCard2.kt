package com.mkumar.ui.components.cards

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun OrderTotalsCard2(
    subtotal: String,
    discount: String,
    other: String,
    grandTotal: String,
    advance: String,
    balance: String,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Order Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(subtotal, {}, readOnly = true, label = { Text("Subtotal") }, modifier = Modifier.weight(1f))
                OutlinedTextField(discount, {}, readOnly = true, label = { Text("Discount") }, modifier = Modifier.weight(1f))
                OutlinedTextField(other, {}, readOnly = true, label = { Text("Other Charges") }, modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(grandTotal, {}, readOnly = true, label = { Text("Grand Total") }, modifier = Modifier.weight(1f))
                OutlinedTextField(advance, {}, readOnly = true, label = { Text("Advance") }, modifier = Modifier.weight(1f))
                OutlinedTextField(balance, {}, readOnly = true, label = { Text("Balance") }, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewOrderTotals2Card() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            OrderTotalsCard2(
                subtotal = "1000",
                discount = "100",
                other = "50",
                grandTotal = "950",
                advance = "500",
                balance = "450"
            )
        }
    }
}

