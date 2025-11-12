package com.mkumar.ui.screens.customer

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AddShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.mkumar.common.constant.CustomerDetailsConstants
import com.mkumar.ui.components.accordions.OrderSummaryAccordion
import com.mkumar.ui.components.bottomsheets.BaseBottomSheet
import com.mkumar.ui.components.bottomsheets.ProductPickerSheet
import com.mkumar.ui.screens.customer.components.CustomerHeader
import com.mkumar.ui.screens.customer.components.OrderList
import com.mkumar.ui.screens.customer.components.OrderSheet
import com.mkumar.ui.screens.customer.model.CustomerHeaderUi
import com.mkumar.ui.screens.customer.model.OrderRowUi
import com.mkumar.viewmodel.CustomerDetailsEffect
import com.mkumar.viewmodel.CustomerDetailsIntent
import com.mkumar.viewmodel.CustomerDetailsViewModel
import com.mkumar.viewmodel.OrderRowAction
import com.mkumar.viewmodel.ProductType
import com.mkumar.viewmodel.UiOrderItem
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailsScreen(
    navController: NavHostController,
    viewModel: CustomerDetailsViewModel,
    onBack: () -> Unit = { navController.popBackStack() },
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    // Bottom sheet state driven by ui.isOrderSheetOpen
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showCustomerDialog by remember { mutableStateOf(false) }
    var showProductPicker by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Snackbar + one-off effects
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                is CustomerDetailsEffect.ShowMessage ->
                    snackbarHostState.showSnackbar(effect.message)

                is CustomerDetailsEffect.OpenOrderSheet ->
                    runCatching { showCustomerDialog = true }

                CustomerDetailsEffect.CloseOrderSheet ->
                    runCatching { showCustomerDialog = false }

                is CustomerDetailsEffect.ViewInvoice -> {
                    val uri = effect.uri
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "application/pdf")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    val chooser = Intent.createChooser(intent, "Open invoice")
                    runCatching { context.startActivity(chooser) }
                        .onFailure { _ ->
                            val hint = humanReadableInvoiceLocation(effect.orderId)
                            snackbarHostState.showSnackbar(
                                "No PDF app found. Invoice saved at: $hint"
                            )
                        }
                }
                is CustomerDetailsEffect.ShareInvoice -> {
                    val send = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, effect.uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        clipData = android.content.ClipData.newRawUri("invoice", effect.uri)
                    }
                    runCatching { context.startActivity(Intent.createChooser(send, "Share invoice")) }
                        .onFailure {
                            snackbarHostState.showSnackbar(
                                "No app to share PDF. File is in Files > Downloads > Documents > MKumar > Invoices"
                            )
                        }
                }
            }
        }
    }

    if (showCustomerDialog){
        BaseBottomSheet(
            title = "Customer Details ${ui.draft.editingOrderId}",
            showTitle = false,
            sheetContent = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 72.dp)
                ) {
                    item {
                        OrderSheet(
                            state = ui,
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (ui.draft.items.isNotEmpty()) {
                            OrderSummaryAccordion(
                                totalAmount = ui.draft.totalAmount,
                                adjustedAmount = ui.draft.adjustedAmount,
                                onAdjustedAmountChange = { viewModel.onIntent(CustomerDetailsIntent.UpdateAdjustedAmount(it)) },
                                advanceTotal = ui.draft.advanceTotal,
                                onAdvanceTotalChange = { viewModel.onIntent(CustomerDetailsIntent.UpdateAdvanceTotal(it)) },
                                remainingBalance = ui.draft.remainingBalance,
                                initiallyExpanded = false
                            )
                        }
                    }
                }
            },
            onDismiss = { viewModel.closeSheet() },
            showDismiss = true,
            onDoneClick = { viewModel.onIntent(CustomerDetailsIntent.SaveDraftAsOrder) },
            showDone = true,
            showAddProduct = true,
            addProductCommonTypes = ProductType.entries.toList(),
            addProductLastUsed = null, // optional if you track it in VM
            onAddProductClick = { type ->
                viewModel.onIntent(CustomerDetailsIntent.AddItem(type))
            },
            onOpenProductPicker = {
                showProductPicker = true
            },
        )
    }

    ProductPickerSheet(
        isOpen = showProductPicker,
        onDismiss = { showProductPicker = false },
        allTypes = ProductType.entries.toList(),
        onAddClick = { type ->
            viewModel.onIntent(CustomerDetailsIntent.AddItem(type))
            showProductPicker = false
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Customer Details",
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
                    joinedAt = ui.customer?.createdAt,
                    totalOrders = ui.orders.size,
                    totalSpent = ui.orders.sumOf { it.totalAmount },
                    totalRemaining = ui.orders.sumOf { it.remainingBalance }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            HorizontalDivider()

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
//                            items = o.items,
                            amount = o.totalAmount,
                            isQueued = false,
                            isSynced = true,
                            hasInvoice = true,
                            remainingBalance = o.remainingBalance,
                            adjustedTotal = o.adjustedAmount
                        )
                    }
                }
                OrderList(
                    orders = rows,
                    onAction = { action ->
                        when (action) {
                            is OrderRowAction.Open -> viewModel.onIntent(CustomerDetailsIntent.OpenOrder(action.orderId))
                            is OrderRowAction.Delete -> viewModel.onIntent(CustomerDetailsIntent.DeleteOrder(action.orderId))
                            is OrderRowAction.Share -> viewModel.onIntent(CustomerDetailsIntent.ShareOrder(action.orderId))
                            is OrderRowAction.ViewInvoice -> viewModel.onIntent(CustomerDetailsIntent.ViewInvoice(action.orderId))
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

fun humanReadableInvoiceLocation(orderId: String): String {
    val fileName = CustomerDetailsConstants.getInvoiceFileName(orderId) + ".pdf"
    return "Files > Downloads > Documents > MKumar > Invoices > $fileName"
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
