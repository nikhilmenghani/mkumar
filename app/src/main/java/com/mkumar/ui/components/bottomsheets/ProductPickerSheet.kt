package com.mkumar.ui.components.bottomsheets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mkumar.model.ProductType
import com.mkumar.ui.meta.productTypeMeta

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductPickerSheet(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    allTypes: List<ProductType>,
    favorites: Set<ProductType> = emptySet(),
    recents: List<ProductType> = emptyList(),
    onAddClick: (ProductType) -> Unit,
    onToggleFavorite: (ProductType, Boolean) -> Unit = { _, _ -> },
) {
    if (!isOpen) return
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = state
    ) {
        var search by remember { mutableStateOf("") }
        val filtered = remember(search, allTypes) {
            val query = search.trim()
            if (query.isEmpty()) allTypes
            else allTypes.filter { type ->
                val meta = productTypeMeta[type]
                val hay = buildString {
                    append(meta?.displayName ?: type.name)
                    append(" ")
                    meta?.tags?.forEach { append(it).append(" ") }
                }
                hay.contains(query, ignoreCase = true)
            }
        }

        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            TextField(
                value = search,
                onValueChange = { search = it },
                label = { Text("Search product type…") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 120.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filtered) { type ->
                    val meta = productTypeMeta[type] ?: return@items
                    Card(
                        onClick = {
                            onAddClick(type)
                            onDismiss()
                        },
                        modifier = Modifier.padding(6.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(meta.icon, contentDescription = meta.displayName)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                meta.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 2
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProductPickerSheetPreview() {
    var open by remember { mutableStateOf(true) }
    ProductPickerSheet(
        isOpen = open,
        onDismiss = { open = false },
        allTypes = ProductType.entries.toList(),
        onAddClick = {}
    )
}
