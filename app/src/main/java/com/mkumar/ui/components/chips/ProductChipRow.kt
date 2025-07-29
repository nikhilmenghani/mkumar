package com.mkumar.ui.components.chips

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.mkumar.data.ProductEntry

@Composable
fun ProductChipRow(
    products: List<ProductEntry>,
    selectedId: String?,
    onChipClick: (String) -> Unit,
    onChipDelete: (String) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(products) { product ->
            ElevatedAssistChip(
                onClick = { onChipClick(product.id) },
                label = {
                    Text("${product.type.label} #${products.indexOf(product) + 1}")
                },
                leadingIcon = if (product.isSaved) {
                    { Icon(Icons.Default.Check, contentDescription = "Saved") }
                } else null,
                trailingIcon = {
                    IconButton(
                        onClick = { onChipDelete(product.id) }
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Delete")
                    }
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (product.id == selectedId)
                        MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surface
                )
            )
        }
    }
}
