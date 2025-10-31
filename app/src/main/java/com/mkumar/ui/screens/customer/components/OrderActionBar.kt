package com.mkumar.ui.screens.customer.components


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mkumar.ui.screens.customer.model.OrderFilterUi


@Composable
fun OrderActionBar(
    filter: OrderFilterUi,
    onFilterChange: (OrderFilterUi) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = filter.query,
            onValueChange = { onFilterChange(filter.copy(query = it)) },
            modifier = Modifier.weight(1f),
            label = { Text("Search orders") },
            singleLine = true
        )
        FilterChip(
            selected = filter.sortNewestFirst,
            onClick = { onFilterChange(filter.copy(sortNewestFirst = !filter.sortNewestFirst)) },
            label = { Text(if (filter.sortNewestFirst) "Newest" else "Oldest") }
        )
        FilledTonalButton(onClick = onRefresh) { Text("Refresh") }
    }
}