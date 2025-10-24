package com.mkumar.ui.components.chips

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mkumar.data.ProductEntry
import com.mkumar.data.ProductFormData
import com.mkumar.data.ProductType

@Composable
fun ProductChipRow(
    products: List<ProductEntry>?,
    selectedId: String?,
    onChipClick: (String) -> Unit,
    onChipDelete: (String) -> Unit,
    getCurrentBuffer: (ProductEntry) -> ProductFormData? = { _ -> null },
    hasUnsavedChanges: (ProductEntry, ProductFormData?) -> Boolean = { _, _ -> false }
) {
    var pendingDeleteId by remember { mutableStateOf<String?>(null) }
    val safeProducts = products.orEmpty()
    if (safeProducts.isEmpty()){
        Box(
            modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
        ) {
            Text(
                text = "No products added yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
            )
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            safeProducts.forEach { product ->
                FilterChip(
                    selected = product.id == selectedId,
                    onClick = { onChipClick(product.id) },
                    label = {
                        Text("${product.productType.label} #${safeProducts.indexOf(product) + 1}")
                    },
                    leadingIcon = {
                        when {
                            product.isSaved && !hasUnsavedChanges(product, getCurrentBuffer(product)) ->
                                Icon(Icons.Default.Check, contentDescription = "Saved")

                            product.isSaved && hasUnsavedChanges(product, getCurrentBuffer(product)) ->
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Unsaved changes",
                                    tint = MaterialTheme.colorScheme.error
                                )

                            else -> null
                        }
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                pendingDeleteId = product.id
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Delete Product",
                                tint = Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    modifier = Modifier.defaultMinSize(minHeight = 36.dp)
                )
            }
        }
    }

    // Confirmation Dialog
    if (pendingDeleteId != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { pendingDeleteId = null },
            title = { Text("Delete Product?") },
            text = { Text("Are you sure you want to delete this product? This action cannot be undone.") },
            confirmButton = {
                Button(onClick = {
                    onChipDelete(pendingDeleteId!!)
                    pendingDeleteId = null
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(onClick = { pendingDeleteId = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
@Preview(showBackground = true)
fun ProductChipRowPreview() {
    val sampleProducts = listOf(
        ProductEntry(id = "1", productType = ProductType.fromLabel("Lens"), isSaved = true),
        ProductEntry(id = "2", productType = ProductType.fromLabel("ContactLens"), isSaved = false),
        ProductEntry(id = "3", productType = ProductType.fromLabel("Frame"), isSaved = true)
    )
    ProductChipRow(
        products = sampleProducts,
        selectedId = "2",
        onChipClick = {},
        onChipDelete = {},
        getCurrentBuffer = { null },
        hasUnsavedChanges = { _, _ -> false }
    )
}


