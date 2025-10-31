package com.mkumar.ui.screens.customer


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mkumar.ui.screens.customer.model.CustomerHeaderUi
import com.mkumar.viewmodel.CustomerDetailsUiState
import java.time.ZoneId
import java.time.format.DateTimeFormatter


@Composable
fun OrderDraftSheet(
    state: CustomerDetailsUiState,
    onSave: () -> Unit,
    onDiscard: () -> Unit,
    onUpdateOccurredAt: (java.time.Instant) -> Unit,
    onRemoveItem: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text("New Sale", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))


// OccurredAt
        val dateFmt = DateTimeFormatter.ofPattern("EEE, MMM d • h:mm a")
        Text("Date/Time: ${dateFmt.format(state.draft.occurredAt.atZone(ZoneId.systemDefault()))}")
        Spacer(Modifier.height(8.dp))


// Items
        if (state.draft.items.isEmpty()) {
            Text("No items added yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            LazyColumn {
                items(state.draft.items, key = { it.id }) { item ->
                    ElevatedCard(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        Column(Modifier.padding(12.dp)) {
                            Text(item.labelOrFallback(), style = MaterialTheme.typography.bodyLarge)
                            Spacer(Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                AssistChip(onClick = {}, label = { Text("Qty ${item.quantity}") })
                                AssistChip(onClick = {}, label = { Text("₹${item.unitPrice}") })
                                if (item.discountPercentage != 0) AssistChip(onClick = {}, label = { Text("${item.discountPercentage}% off") })
                                Spacer(Modifier.weight(1f))
                                TextButton(onClick = { onRemoveItem(item.id) }) { Text("Remove") }
                            }
                        }
                    }
                }
            }
        }


        Spacer(Modifier.height(12.dp))
// Totals
        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                TotalRow("Subtotal", state.draft.subtotalBeforeAdjust)
                TotalRow("Adjusted", -state.draft.adjustedAmount)
                TotalRow("Advance", -state.draft.advanceTotal)
                Divider(Modifier.padding(vertical = 6.dp))
                TotalRow("Grand Total", state.draft.totalAmount, emphasize = true)
                TotalRow("Remaining", state.draft.remainingBalance)
            }
        }


        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onDiscard, modifier = Modifier.weight(1f)) { Text("Discard") }
            Button(onClick = onSave, enabled = state.draft.items.isNotEmpty(), modifier = Modifier.weight(1f)) { Text("Save Order") }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun TotalRow(label: String, amount: Int, emphasize: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = if (emphasize) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium)
        Text("₹$amount", style = if (emphasize) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium)
    }
}