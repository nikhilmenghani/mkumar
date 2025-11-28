package com.mkumar.ui.screens.customer.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mkumar.common.extension.nowUtcMillis
import com.mkumar.model.OrderRowAction
import com.mkumar.model.OrderRowUi

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

@Preview(showBackground = true)
@Composable
fun OrderListPreview() {
    val sampleOrders = listOf(
        OrderRowUi(
            id = "1",
            receivedAt = nowUtcMillis(),
            invoiceNumber = "2",
            amount = 1250,
            remainingBalance = 300,
            lastUpdatedAt = nowUtcMillis(),
            adjustedTotal = 1200
        ),
        OrderRowUi(
            id = "2",
            receivedAt = nowUtcMillis(),
            invoiceNumber = "103",
            amount = 750,
            lastUpdatedAt = nowUtcMillis(),
            remainingBalance = 0
        )
    )
    OrderList(
        orders = sampleOrders,
        onAction = {},
        modifier = Modifier
    )
}
