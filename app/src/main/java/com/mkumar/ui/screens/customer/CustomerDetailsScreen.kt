// =============================
// Phase 5 — UI Layer Components
// CustomerDetails screen + Order list + BottomSheet with Product accordion & forms
// Packages assume: com.mkumar.ui.screens.customer + subpackages
// Compose BOM 2025.02, Material3, Kotlin 2.1.x
// =============================

// -----------------------------
// File: ui/screens/customer/CustomerDetailsScreen.kt
// -----------------------------
package com.mkumar.ui.screens.customer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AddShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.mkumar.ui.screens.customer.components.CustomerHeader
import com.mkumar.ui.screens.customer.components.OrderActionBar
import com.mkumar.ui.screens.customer.components.OrderList
import com.mkumar.ui.screens.customer.model.*
import com.mkumar.viewmodel.CustomerDetailsEffect
import com.mkumar.viewmodel.CustomerDetailsIntent
import com.mkumar.viewmodel.CustomerDetailsViewModel
import com.mkumar.viewmodel.UiOrderItem
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailsScreen(
    navController: NavHostController,
    viewModel: CustomerDetailsViewModel,
    onBack: () -> Unit = { navController.popBackStack() },
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    // Snackbar + one-off effects
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is CustomerDetailsEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
                CustomerDetailsEffect.OpenOrderSheet -> { /* handled by state flag below */ }
                CustomerDetailsEffect.CloseOrderSheet -> { /* handled by state flag below */ }
            }
        }
    }

    // Bottom sheet state driven by ui.isOrderSheetOpen
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    if (ui.isOrderSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.onIntent(CustomerDetailsIntent.CloseSheet) },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            OrderDraftSheet(
                state = ui,
                onSave = { viewModel.onIntent(CustomerDetailsIntent.SaveDraftAsOrder) },
                onDiscard = { viewModel.onIntent(CustomerDetailsIntent.DiscardDraft) },
                onUpdateOccurredAt = { viewModel.onIntent(CustomerDetailsIntent.UpdateOccurredAt(it)) },
                onRemoveItem = { id -> viewModel.onIntent(CustomerDetailsIntent.RemoveItem(id)) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        ui.customer?.name ?: "Customer",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.onIntent(CustomerDetailsIntent.NewSale) },
                icon = { Icon(Icons.Outlined.AddShoppingCart, contentDescription = null) },
                text = { Text("New Sale") }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (ui.isLoading) LinearProgressIndicator(Modifier.fillMaxWidth())

            // Header
            CustomerHeader(
                header = CustomerHeaderUi(
                    id = ui.customer?.id.orEmpty(),
                    name = ui.customer?.name.orEmpty(),
                    phoneFormatted = ui.customer?.phone.orEmpty(),
                    joinedAt = null,
                    totalOrders = ui.orders.size,
                    totalSpent = ui.orders.sumOf { it.totalAmount }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Actions
            OrderActionBar(
                filter = OrderFilterUi(), // Hook up when you add query/sort to state
                onFilterChange = { /* no-op for now */ },
                onRefresh = { viewModel.onIntent(CustomerDetailsIntent.Refresh) },
                modifier = Modifier.fillMaxWidth()
            )

            Divider()

            if (ui.orders.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "No orders yet. Tap ‘New Sale’ to create the first one.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Reuse existing list UI by adapting to OrderRowUi
                val rows = remember(ui.orders) {
                    ui.orders.map { o ->
                        OrderRowUi(
                            id = o.id,
                            occurredAt = o.occurredAt,
                            itemsLabel = if (o.items.isEmpty()) "—" else o.items.joinToString { it.labelOrFallback() },
                            amount = o.totalAmount,
                            isQueued = false,
                            isSynced = true,
                            hasInvoice = true
                        )
                    }
                }
                OrderList(
                    orders = rows,
                    onAction = { /* wire when you implement actions */ },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

// Helper for item label rendering without depending on exact UiOrderItem fields
fun UiOrderItem.labelOrFallback(): String =
    buildString {
        // Try best-effort fields; fallback to quantity×price
        // If your UiOrderItem has fields like `title`/`description`/`brand`, prefer them.
        try {
            val cls = this@labelOrFallback::class
            val title = runCatching { cls.members.firstOrNull { it.name == "description" }?.call(this@labelOrFallback) as? String }.getOrNull()
                ?: runCatching { cls.members.firstOrNull { it.name == "brand" }?.call(this@labelOrFallback) as? String }.getOrNull()
                ?: "Item"
            append(title)
        } catch (_: Throwable) {
            append("Item")
        }
        append(" (")
        append(quantity)
        append("×₹")
        append(unitPrice)
        append(")")
    }
