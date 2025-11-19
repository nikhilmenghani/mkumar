package com.mkumar.ui.components.fabs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mkumar.model.ProductType
import com.mkumar.ui.meta.productTypeMeta

@Composable
fun AddProductSpeedMenuButton(
    commonTypes: List<ProductType>,
    lastUsed: ProductType?,
    onAddClick: (ProductType) -> Unit,
    onOpenPicker: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var width by remember { mutableStateOf(180.dp) }
    Box {
//        ExtendedFloatingActionButton(
//            onClick = {
//                if (lastUsed != null) onAddClick(lastUsed) else expanded = true
//            },
//            icon = { Icon(Icons.Filled.Add, contentDescription = "Add Product") },
//            text = { Text("Add Product") },
//            containerColor = MaterialTheme.colorScheme.surface,
//            contentColor = MaterialTheme.colorScheme.onSurface,
//            modifier = Modifier.size(width = width, height = 56.dp)
//        )

        FloatingActionButton(
            onClick = {
                if (lastUsed != null) onAddClick(lastUsed) else expanded = true
            },
            modifier = Modifier.size(56.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) { Icon(Icons.Filled.Add, contentDescription = "Add Product") }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(width)
        ) {
            commonTypes.forEach { type ->
                DropdownMenuItem(
                    text = { Text(productTypeMeta[type]?.displayName ?: type.name) },
                    onClick = {
                        expanded = false
                        onAddClick(type)
                    }
                )
            }
            DropdownMenuItem(
                text = { Text("More…") },
                onClick = {
                    expanded = false
                    onOpenPicker()
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AddProductSpeedMenuButtonPreview() {
    AddProductSpeedMenuButton(
        commonTypes = ProductType.entries.toList(),
        lastUsed = ProductType.Frame,
        onAddClick = {},
        onOpenPicker = {}
    )
}
