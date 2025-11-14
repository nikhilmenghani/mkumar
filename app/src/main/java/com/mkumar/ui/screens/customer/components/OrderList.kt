package com.mkumar.ui.screens.customer.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mkumar.common.extension.toLong
import com.mkumar.viewmodel.OrderRowAction
import com.mkumar.viewmodel.OrderRowUi
import java.time.Instant

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
            occurredAt = Instant.now().toLong(),
            invoiceNumber = "2",
            amount = 1250,
            remainingBalance = 300,
            lastUpdatedAt = Instant.now().toLong(),
            adjustedTotal = 1200
        ),
        OrderRowUi(
            id = "2",
            occurredAt = Instant.now().toLong(),
            invoiceNumber = "103",
            amount = 750,
            lastUpdatedAt = Instant.now().toLong(),
            remainingBalance = 0
        )
    )
    OrderList(
        orders = sampleOrders,
        onAction = {},
        modifier = Modifier
    )
}
