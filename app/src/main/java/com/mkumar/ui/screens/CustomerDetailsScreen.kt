package com.mkumar.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AddShoppingCart
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.mkumar.data.CustomerHeaderUi
import com.mkumar.data.OrderSummaryUi
import com.mkumar.ui.components.bottomsheets.BaseBottomSheet
import com.mkumar.viewmodel.CustomerDetailsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailsScreen(
    navController: NavHostController,
    customerDetailsViewModel: CustomerDetailsViewModel,
    onDismissError: () -> Unit = {}
) {
    var showCustomerDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customer") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    customerDetailsViewModel.onNewSale()
                },
                icon = { Icon(Icons.Outlined.AddShoppingCart, contentDescription = null) },
                text = { Text("New Sale") }
            )
        }
    ) { padding ->

        val state by customerDetailsViewModel.ui.collectAsStateWithLifecycle()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.isLoading) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }

            if (state.error != null) {
                ErrorBanner(message = state.error!!, onDismiss = onDismissError)
            }

            state.header?.let { HeaderCard(it) }

            if (state.ordersByDay.isEmpty() && !state.isLoading) {
                EmptyOrders()
            } else {
                OrdersList(state.ordersByDay, onOrderClick = {
                    showCustomerDialog = true
                })
            }
        }
    }
    if (showCustomerDialog) {
        BaseBottomSheet(
            title = "Customer Details",
            sheetContent = {
                val scrollState = rememberScrollState()
                // Content for customer details
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .verticalScroll(scrollState)
                        .padding(bottom = 72.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ){
                    Text("Customer details go here", modifier = Modifier.padding(16.dp))
                }
            },
            onDismiss = { showCustomerDialog = false },
            showDismiss = true,
        )
    }
}

@Composable
private fun HeaderCard(header: CustomerHeaderUi) {
    ElevatedCard(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                header.displayName,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text("Phone: ${header.phoneFormatted}", style = MaterialTheme.typography.bodyMedium)

            // Metrics row (show only if available)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                header.totalOrders?.let {
                    Text(
                        "Total Orders: $it",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                header.lifetimeValueFormatted?.let {
                    Text(
                        "Lifetime Value: $it",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            header.lastVisitFormatted?.let {
                Text("Last Visit: $it", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun OrdersList(
    ordersByDay: Map<String, List<OrderSummaryUi>>,
    onOrderClick: (String) -> Unit
) {
    // Debug: Log all order IDs
    val allIds = ordersByDay.values.flatten().map { it.id }
    Log.d("OrdersList", "Order IDs: $allIds")

    // Check for duplicates
    val duplicateIds = allIds.groupBy { it }.filter { it.value.size > 1 }.keys
    if (duplicateIds.isNotEmpty()) {
        Log.e("OrdersList", "Duplicate order IDs found: $duplicateIds")
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ordersByDay.forEach { (day, orders) ->
            item(key = "day-$day") {
                Text(
                    day,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }
            items(orders, key = { it.id }) { o ->
                OrderRow(o) { onOrderClick(o.id) }
            }
        }
    }
}

@Composable
private fun OrderRow(o: OrderSummaryUi, onClick: () -> Unit) {
    ElevatedCard(onClick = onClick) {
        ListItem(
            headlineContent = {
                Text(o.invoiceShort, maxLines = 1, overflow = TextOverflow.Ellipsis)
            },
            supportingContent = {
                Text(
                    "${o.subtitle} • ${o.timeFormatted}",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            trailingContent = {
                if (o.isDraft) {
                    AssistChip(onClick = {}, label = { Text("Draft") }, enabled = false)
                } else {
                    Text(o.totalFormatted ?: "")
                }
            }
        )
    }
}

@Composable
private fun EmptyOrders() {
    Box(Modifier
        .fillMaxWidth()
        .padding(24.dp), contentAlignment = Alignment.Center) {
        Text(
            "No orders yet for this customer. Tap “New Sale”.",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun ErrorBanner(message: String, onDismiss: () -> Unit) {
    Surface(tonalElevation = 2.dp, color = MaterialTheme.colorScheme.errorContainer) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                message,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onDismiss) { Text("Dismiss") }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHeaderCard() {
    HeaderCard(
        header = CustomerHeaderUi(
            displayName = "Jane Doe",
            phoneFormatted = "+1 555-1234",
            totalOrders = 12,
            lifetimeValueFormatted = "$1,200.00",
            lastVisitFormatted = "Apr 10, 2024",
            id = "test"
        )
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewOrdersList() {
    val sampleOrders = listOf(
        OrderSummaryUi(
            id = "1",
            invoiceShort = "INV-001",
            subtitle = "First order",
            timeFormatted = "10:00 AM",
            isDraft = false,
            totalFormatted = "$100.00"
        ),
        OrderSummaryUi(
            id = "2",
            invoiceShort = "INV-002",
            subtitle = "Second order",
            timeFormatted = "11:30 AM",
            isDraft = true,
            totalFormatted = null
        )
    )
    OrdersList(
        ordersByDay = mapOf("Today" to sampleOrders),
        onOrderClick = {}
    )
}

