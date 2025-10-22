package com.mkumar.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AddShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.mkumar.data.CustomerDetailsUiState
import com.mkumar.data.CustomerHeaderUi
import com.mkumar.data.OrderSummaryUi
import com.mkumar.viewmodel.CustomerDetailsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailsScreen(
    navController: NavHostController,
    customerDetailsViewModel: CustomerDetailsViewModel,
    state: CustomerDetailsUiState = CustomerDetailsUiState(),
    onNewSale: () -> Unit = {},
    onOrderClick: (String) -> Unit = {},
    onDismissError: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.header?.displayName ?: "Customer") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNewSale,
                icon = { Icon(Icons.Outlined.AddShoppingCart, contentDescription = null) },
                text = { Text("New Sale") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.isLoading) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }

            if (state.error != null) {
                ErrorBanner(message = state.error, onDismiss = onDismissError)
            }

            state.header?.let { HeaderCard(it) }

            if (state.ordersByDay.isEmpty() && !state.isLoading) {
                EmptyOrders()
            } else {
                OrdersList(state.ordersByDay, onOrderClick)
            }
        }
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
