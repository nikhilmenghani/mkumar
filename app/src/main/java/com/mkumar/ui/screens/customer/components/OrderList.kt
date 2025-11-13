package com.mkumar.ui.screens.customer.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mkumar.ui.screens.customer.model.OrderRowUi
import com.mkumar.viewmodel.OrderRowAction

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
            OrderListItem4(row = row, onAction = onAction)
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun OrderListPreview() {
    val sampleOrders = listOf(
        OrderRowUi(
            id = "1",
            occurredAt = java.time.Instant.now(),
            invoiceNumber = "2",
//            items = emptyList(),
            amount = 1250,
            hasInvoice = true,
            isQueued = false,
            isSynced = true,
            remainingBalance = 300
        ),
        OrderRowUi(
            id = "2",
            occurredAt = java.time.Instant.now(),
            invoiceNumber = "103",
//            items = emptyList(),
            amount = 750,
            hasInvoice = false,
            isQueued = true,
            isSynced = false,
            remainingBalance = 0
        )
    )
    OrderList(
        orders = sampleOrders,
        onAction = {},
        modifier = Modifier
    )
}
