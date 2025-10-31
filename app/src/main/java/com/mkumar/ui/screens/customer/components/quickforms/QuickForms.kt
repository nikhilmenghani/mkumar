package com.mkumar.ui.components.quickforms

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun LensQuickForm(
    description: String, onDescription: (String) -> Unit,
    qty: String, onQty: (String) -> Unit,
    price: String, onPrice: (String) -> Unit,
    off: String, onOff: (String) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    QuickFormScaffold(
        nameLabel = "Lens description",
        name = description, onName = onDescription,
        qty = qty, onQty = onQty,
        price = price, onPrice = onPrice,
        off = off, onOff = onOff,
        onAdd = onAdd,
        modifier = modifier
    )
}

@Composable
fun FrameQuickForm(
    model: String, onModel: (String) -> Unit,
    qty: String, onQty: (String) -> Unit,
    price: String, onPrice: (String) -> Unit,
    off: String, onOff: (String) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    QuickFormScaffold(
        nameLabel = "Frame model",
        name = model, onName = onModel,
        qty = qty, onQty = onQty,
        price = price, onPrice = onPrice,
        off = off, onOff = onOff,
        onAdd = onAdd,
        modifier = modifier
    )
}

@Composable
fun ContactLensQuickForm(
    brand: String, onBrand: (String) -> Unit,
    qty: String, onQty: (String) -> Unit,
    price: String, onPrice: (String) -> Unit,
    off: String, onOff: (String) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    QuickFormScaffold(
        nameLabel = "Contact lens brand",
        name = brand, onName = onBrand,
        qty = qty, onQty = onQty,
        price = price, onPrice = onPrice,
        off = off, onOff = onOff,
        onAdd = onAdd,
        modifier = modifier
    )
}

@Composable
private fun QuickFormScaffold(
    nameLabel: String,
    name: String, onName: (String) -> Unit,
    qty: String, onQty: (String) -> Unit,
    price: String, onPrice: (String) -> Unit,
    off: String, onOff: (String) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val qtyInt = qty.toIntOrNull() ?: 0
    val priceInt = price.toIntOrNull() ?: 0
    val offInt = (off.toIntOrNull() ?: 0).coerceIn(0, 100)
    val isAddEnabled = qtyInt > 0 && priceInt >= 0

    Column(modifier = modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = name, onValueChange = onName,
            label = { Text(nameLabel) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = qty, onValueChange = onQty,
                label = { Text("Qty") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = price, onValueChange = onPrice,
                label = { Text("Unit ₹") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = off, onValueChange = onOff,
                label = { Text("Off %") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                supportingText = { Text("$offInt%") },
                modifier = Modifier.weight(1f)
            )
        }
        Button(
            onClick = onAdd,
            enabled = isAddEnabled,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Add item") }
    }
}
