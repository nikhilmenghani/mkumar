package com.mkumar.ui.screens.customer.components


import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mkumar.ui.screens.customer.model.OrderRowAction
import com.mkumar.ui.screens.customer.model.OrderRowUi


@Composable
fun OrderList(
    orders: List<OrderRowUi>,
    onAction: (OrderRowAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 96.dp),
        modifier = modifier
    ) {
        items(orders, key = { it.id }) { row ->
            OrderListItem(row = row, onAction = onAction)
        }
    }
}