package com.mkumar.ui.components.selectors

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mkumar.data.ProductType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductSelector(
    availableTypes: List<ProductType>,
    selectedType: ProductType?,
    onTypeSelected: (ProductType) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
//    var selectedType by remember { mutableStateOf(availableTypes.firstOrNull()) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.weight(1f)
        ) {
            OutlinedTextField(
                readOnly = true,
                value = selectedType?.label ?: "",
                onValueChange = {},
                label = { Text("Product Type") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                availableTypes.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.label) },
                        onClick = {
                            onTypeSelected(type)
                            expanded = false
                        }
                    )
                }
            }
        }

//        IconButton(
//            onClick = { selectedType?.let { onAddClick(it) } },
//            enabled = selectedType != null,
//            modifier = Modifier.align(Alignment.CenterVertically)
//        ) {
//            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Product")
//        }
    }
}
