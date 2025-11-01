package com.mkumar.ui.screens.customer


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mkumar.ui.components.quickforms.ContactLensQuickForm
import com.mkumar.ui.components.quickforms.FrameQuickForm
import com.mkumar.ui.components.quickforms.LensQuickForm
import com.mkumar.ui.screens.customer.components.ProductTypePicker
import com.mkumar.ui.screens.customer.model.ProductType
import com.mkumar.viewmodel.CustomerDetailsIntent
import com.mkumar.viewmodel.CustomerDetailsUiState
import com.mkumar.viewmodel.CustomerDetailsViewModel
import com.mkumar.viewmodel.UiOrderItem
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter


@Composable
fun OrderDraftSheet(
    state: CustomerDetailsUiState,
    onSave: () -> Unit,
    onDiscard: () -> Unit,
    onUpdateOccurredAt: (Instant) -> Unit,
    onRemoveItem: (String) -> Unit,
    viewModel: CustomerDetailsViewModel,
    modifier: Modifier = Modifier
) {
// --- Local UI state for the product forms ---
    var selected by remember { mutableStateOf<ProductType?>(ProductType.LENS) }
    var lensDesc by remember { mutableStateOf("") }
    var lensQty by remember { mutableStateOf("1") }
    var lensPrice by remember { mutableStateOf("0") }
    var lensOff by remember { mutableStateOf("0") }


    var frameModel by remember { mutableStateOf("") }
    var frameQty by remember { mutableStateOf("1") }
    var framePrice by remember { mutableStateOf("0") }
    var frameOff by remember { mutableStateOf("0") }


    var clBrand by remember { mutableStateOf("") }
    var clQty by remember { mutableStateOf("1") }
    var clPrice by remember { mutableStateOf("0") }
    var clOff by remember { mutableStateOf("0") }


    Column(modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text("New Sale", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))


// OccurredAt
        val dateFmt = DateTimeFormatter.ofPattern("EEE, MMM d • h:mm a")
        Text("Date/Time: ${dateFmt.format(state.draft.occurredAt.atZone(ZoneId.systemDefault()))}")
        Spacer(Modifier.height(12.dp))

// ---------- Add products section ----------
        Text("Add products", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        ProductTypePicker(selected = selected, onSelected = { selected = it })
        Spacer(Modifier.height(8.dp))


        when (selected) {
            ProductType.LENS -> LensQuickForm(
                description = lensDesc, onDescription = { lensDesc = it },
                qty = lensQty, onQty = { lensQty = it },
                price = lensPrice, onPrice = { lensPrice = it },
                off = lensOff, onOff = { lensOff = it },
                onAdd = {
                    val item = uiItem(
                        title = if (lensDesc.isNotBlank()) "Lens: $lensDesc" else "Lens",
                        qty = lensQty.toIntOrNull() ?: 0,
                        price = lensPrice.toIntOrNull() ?: 0,
                        off = (lensOff.toIntOrNull() ?: 0).coerceIn(0, 100),
                        type = ProductType.LENS
                    )
                    viewModel.onIntent(CustomerDetailsIntent.AddItem(item))
                }
            )

            ProductType.FRAME -> FrameQuickForm(
                model = frameModel, onModel = { frameModel = it },
                qty = frameQty, onQty = { frameQty = it },
                price = framePrice, onPrice = { framePrice = it },
                off = frameOff, onOff = { frameOff = it },
                onAdd = {
                    val item = uiItem(
                        title = if (frameModel.isNotBlank()) "Frame: $frameModel" else "Frame",
                        qty = frameQty.toIntOrNull() ?: 0,
                        price = framePrice.toIntOrNull() ?: 0,
                        off = (frameOff.toIntOrNull() ?: 0).coerceIn(0, 100),
                        type = ProductType.FRAME
                    )
                    CustomerDetailsIntent.AddItem(item)
                }
            )

            ProductType.CONTACT_LENS -> ContactLensQuickForm(
                brand = clBrand, onBrand = { clBrand = it },
                qty = clQty, onQty = { clQty = it },
                price = clPrice, onPrice = { clPrice = it },
                off = clOff, onOff = { clOff = it },
                onAdd = {
                    val item = uiItem(
                        title = if (clBrand.isNotBlank()) "Contacts: $clBrand" else "Contacts",
                        qty = clQty.toIntOrNull() ?: 0,
                        price = clPrice.toIntOrNull() ?: 0,
                        off = (clOff.toIntOrNull() ?: 0).coerceIn(0, 100),
                        type = ProductType.CONTACT_LENS
                    )
                    CustomerDetailsIntent.AddItem(item)
                }
            )

            null -> {}
        }

        // ---------- Items list ----------
        Spacer(Modifier.height(16.dp))
        if (state.draft.items.isEmpty()) {
            Text("No items added yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            LazyColumn {
                items(state.draft.items, key = { it.id }) { item ->
                    ElevatedCard(Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)) {
                        Column(Modifier.padding(12.dp)) {
                            Text(itemLabel(item), style = MaterialTheme.typography.bodyLarge)
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

// -----------------------------
// Helpers used inside OrderDraftSheet (UI-only)
// -----------------------------


/**
 * Create a UiOrderItem with best-guess constructor. Adjust the constructor/fields
 * to your actual data class if needed.
 */
private fun uiItem(
    title: String,
    qty: Int,
    price: Int,
    off: Int,
    type: ProductType
): UiOrderItem {
    return UiOrderItem(
        id = "", // VM will assign/ensure a stable id
        productType = type,
        name = title,
        quantity = qty,
        unitPrice = price,
        discountPercentage = off.coerceIn(0, 100)
    )
}


/** Render a friendly label for the item in the draft list. */
private fun itemLabel(item: UiOrderItem): String =
    buildString {
// Prefer a descriptive field if your UiOrderItem has one
// (e.g., description/model/brand). Otherwise fallback.
        val guess = runCatching {
            val k = item::class
            (k.members.firstOrNull { it.name == "description" }?.call(item) as? String)
                ?: (k.members.firstOrNull { it.name == "brand" }?.call(item) as? String)
        }.getOrNull()
        append(guess ?: "Item")
        append(" (")
        append(item.quantity)
        append("×₹")
        append(item.unitPrice)
        append(")")
    }