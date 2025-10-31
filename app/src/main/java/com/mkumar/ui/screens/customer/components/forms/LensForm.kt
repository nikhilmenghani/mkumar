package com.mkumar.ui.screens.customer.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mkumar.ui.screens.customer.model.LensFormState

@Composable
fun LensForm(
    state: LensFormState,
    onChange: (LensFormState) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = state.description,
            onValueChange = { onChange(state.copy(description = it)) },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = state.quantity.toString(),
                onValueChange = { onChange(state.copy(quantity = it.toIntOrNull() ?: 0)) },
                label = { Text("Qty") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = state.unitPrice.toString(),
                onValueChange = { onChange(state.copy(unitPrice = it.toIntOrNull() ?: 0)) },
                label = { Text("Unit Price (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = state.discountPercentage.toString(),
                onValueChange = { onChange(state.copy(discountPercentage = (it.toIntOrNull() ?: 0).coerceIn(0, 100))) },
                label = { Text("% Off") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }
    }
}