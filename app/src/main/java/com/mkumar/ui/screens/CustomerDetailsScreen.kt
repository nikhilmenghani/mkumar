//package com.mkumar.ui.screens
//
//import androidx.compose.foundation.BorderStroke
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.PaddingValues
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.automirrored.filled.ArrowBack
//import androidx.compose.material.icons.outlined.AddShoppingCart
//import androidx.compose.material3.AssistChip
//import androidx.compose.material3.CardDefaults
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.ExtendedFloatingActionButton
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.LinearProgressIndicator
//import androidx.compose.material3.ListItem
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.OutlinedCard
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextButton
//import androidx.compose.material3.TopAppBar
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableIntStateOf
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.lifecycle.compose.collectAsStateWithLifecycle
//import androidx.navigation.NavHostController
//import com.mkumar.data.CustomerHeaderUi
//import com.mkumar.data.OrderSummaryUi
//import com.mkumar.data.ProductEntry
//import com.mkumar.data.ProductType
//import com.mkumar.ui.components.bottomsheets.BaseBottomSheet
//import com.mkumar.ui.components.cards.OrderTotalsCard
//import com.mkumar.ui.theme.AppColors
//import com.mkumar.viewmodel.CustomerDetailsViewModel
//import java.util.UUID
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun CustomerDetailsScreen(
//    navController: NavHostController,
//    customerDetailsViewModel: CustomerDetailsViewModel,
//    onDismissError: () -> Unit = {}
//) {
//    var showCustomerDialog by remember { mutableStateOf(false) }
//    var selectedOrder by remember { mutableStateOf<OrderSummaryUi?>(null) }
//    val state by customerDetailsViewModel.ui.collectAsStateWithLifecycle()
//    val customerName = state.header?.displayName ?: "Customer"
//    val phoneNumber = state.header?.phoneFormatted ?: ""
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Customer") },
//                navigationIcon = {
//                    IconButton(onClick = { navController.popBackStack() }) {
//                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
//                    }
//                }
//            )
//        },
//        floatingActionButton = {
//            ExtendedFloatingActionButton(
//                onClick = {
//                    showCustomerDialog = true
//                    selectedOrder = customerDetailsViewModel.createNewOrder()
//                },
//                icon = { Icon(Icons.Outlined.AddShoppingCart, contentDescription = null) },
//                text = { Text("New Sale") }
//            )
//        }
//    ) { padding ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(padding)
//        ) {
//            if (state.isLoading) {
//                LinearProgressIndicator(Modifier.fillMaxWidth())
//            }
//
//            if (state.error != null) {
//                ErrorBanner(message = state.error!!, onDismiss = onDismissError)
//            }
//
//            state.header?.let { HeaderCard(it) }
//
//            if (state.ordersByDay.isEmpty() && !state.isLoading) {
//                EmptyOrders()
//            } else {
//                OrdersList(
//                    state.ordersByDay,
//                    onSaveClick = { id ->
//                        val selectedOrderId = id
//                        customerDetailsViewModel.saveProductsToOrder(
//                            selectedOrderId,
//                            customerDetailsViewModel.getOrderById(selectedOrderId)
//                        )
//                    },
//                    onDeleteClick = { id ->
//                        customerDetailsViewModel.deleteOrder(id)
//                    },
//                    onOrderClick = { order ->
//                        showCustomerDialog = true
//                        selectedOrder = order
//                    })
//            }
//        }
//    }
//    if (showCustomerDialog) {
//        val selectedOrderId = selectedOrder?.id ?: UUID.randomUUID().toString()
//        val latestSelectedOrder = customerDetailsViewModel.getOrderById(selectedOrderId)
//        OrderEntryAccordionScreenWrapper(
//            customerName = customerName,
//            phoneNumber = phoneNumber,
//            selectedOrder = latestSelectedOrder,
//            customerDetailsViewModel = customerDetailsViewModel,
//            onDismiss = { showCustomerDialog = false }
//        )
//    }
//}
//
//@Composable
//private fun OrderEntryAccordionScreenWrapper(
//    customerName: String,
//    phoneNumber: String,
//    selectedOrder: OrderSummaryUi?,
//    customerDetailsViewModel: CustomerDetailsViewModel,
//    onDismiss: () -> Unit
//) {
//    val selectedProductType = remember { mutableStateOf<ProductType?>(null) }
//    val selectedOrderId = selectedOrder?.id ?: ""
//
//    val totalAmount = selectedOrder?.products?.sumOf { it.finalTotal } ?: 0
//    var advanceTotal = selectedOrder?.advanceTotal ?: 0
//    var adjustedAmount by remember(selectedOrderId) {
//        mutableIntStateOf(selectedOrder?.adjustedAmount ?: advanceTotal)
//    }
//    val remainingBalance = adjustedAmount - advanceTotal
//
//    BaseBottomSheet(
//        title = "Customer Details ${selectedOrder?.id}",
//        showTitle = false,
//        sheetContent = {
//            LazyColumn(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 2.dp),
//                verticalArrangement = Arrangement.spacedBy(16.dp),
//                contentPadding = PaddingValues(bottom = 72.dp)
//            ) {
//                item {
//                    OrderEntryAccordionScreen(
//                        customerName = customerName,
//                        phoneNumber = phoneNumber,
//                        products = selectedOrder?.products,
//                        getProductFormData = { product -> customerDetailsViewModel.getProductFormData(selectedOrderId, product) },
//                        updateProductFormData = { productId, data -> customerDetailsViewModel.updateProductFormData(selectedOrderId, productId, data) },
//                        onOwnerChange = { productId, newName -> customerDetailsViewModel.updateProductOwnerName(selectedOrderId, productId, newName) },
//                        hasUnsavedChanges = { product, buf -> customerDetailsViewModel.hasUnsavedChanges(selectedOrderId, product, buf) },
//                        onFormSave = { productId, data ->
//                            customerDetailsViewModel.saveProductFormData(selectedOrderId, productId, data)
//                            selectedProductType.value = null
//                        },
//                        availableTypes = ProductType.allTypes,
//                        selectedType = selectedProductType.value,
//                        onTypeSelected = { selectedProductType.value = it },
//                        onAddClick = {
//                            val newProductEntry = ProductEntry(
//                                productType = selectedProductType.value!!,
//                                productOwnerName = customerName
//                            )
//                            selectedOrder?.let { order ->
//                                customerDetailsViewModel.addProductToOrder(order.id, newProductEntry)
//                                selectedProductType.value = null
//                            }
//                        },
//                    )
//                }
//                if (selectedOrder?.products?.any { it.isSaved } == true) {
//                    item {
//                        OrderTotalsCard(
//                            onAdvanceTotalChange = { newAdvanceTotal ->
//                                advanceTotal = newAdvanceTotal
//                                customerDetailsViewModel.updateOrderTotals(selectedOrderId, totalAmount, adjustedAmount, newAdvanceTotal, remainingBalance)
//                            },
//                            totalAmount = totalAmount,
//                            initialAdvanceTotal = advanceTotal,
//                            adjustedAmount = adjustedAmount,
//                            onAdjustedAmountChange = { newAdjustedAmount ->
//                                adjustedAmount = newAdjustedAmount
//                                customerDetailsViewModel.updateOrderTotals(selectedOrderId, totalAmount, newAdjustedAmount, advanceTotal, remainingBalance)
//                            }
//                        )
//                    }
//                }
//            }
//        },
//        onDismiss = onDismiss,
//        showDismiss = true
//    )
//}
//
//@Composable
//private fun HeaderCard(header: CustomerHeaderUi) {
//    OutlinedCard(
//        colors = AppColors.outlinedCardColors(),
//        border = BorderStroke(1.dp, Color.Black),
//        modifier = Modifier
//            .padding(horizontal = 16.dp, vertical = 12.dp)
//            .fillMaxWidth()
//    ) {
//        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
//            Text(
//                header.displayName,
//                style = MaterialTheme.typography.titleLarge,
//                maxLines = 1,
//                overflow = TextOverflow.Ellipsis
//            )
//            Text("Phone: ${header.phoneFormatted}", style = MaterialTheme.typography.bodyMedium)
//
//            // Metrics row (show only if available)
//            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
//                header.totalOrders?.let {
//                    Text(
//                        "Total Orders: $it",
//                        style = MaterialTheme.typography.bodyMedium
//                    )
//                }
//                header.lifetimeValueFormatted?.let {
//                    Text(
//                        "Lifetime Value: $it",
//                        style = MaterialTheme.typography.bodyMedium
//                    )
//                }
//            }
//            header.lastVisitFormatted?.let {
//                Text("Last Visit: $it", style = MaterialTheme.typography.bodyMedium)
//            }
//        }
//    }
//}
//
//@Composable
//private fun OrdersList(
//    ordersByDay: Map<String, List<OrderSummaryUi>>,
//    onSaveClick: (String) -> Unit = {},
//    onDeleteClick: (String) -> Unit = {},
//    onOrderClick: (OrderSummaryUi) -> Unit
//) {
//    LazyColumn(
//        modifier = Modifier.fillMaxSize(),
//        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
//        verticalArrangement = Arrangement.spacedBy(12.dp)
//    ) {
//        ordersByDay.forEach { (day, orders) ->
//            item(key = "day-$day") {
//                Text(
//                    day,
//                    style = MaterialTheme.typography.titleMedium,
//                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
//                )
//            }
//            items(orders, key = { it.id }) { o ->
//                OrderRow(
//                    o,
//                    onSaveClick = onSaveClick,
//                    onDeleteClick = onDeleteClick
//                ) { onOrderClick(o) }
//            }
//        }
//    }
//}
//
//@Composable
//private fun OrderRow(
//    o: OrderSummaryUi,
//    onSaveClick: (String) -> Unit = {},
//    onDeleteClick: (String) -> Unit = {},
//    onClick: () -> Unit
//) {
//    OutlinedCard(
//        onClick = onClick,
//        shape = RoundedCornerShape(16.dp),
//        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
//        colors = AppColors.outlinedCardColors(),
//    ) {
//        ListItem(
//            headlineContent = {
//                Text(o.invoiceShort, maxLines = 1, overflow = TextOverflow.Ellipsis)
//            },
//            supportingContent = {
//                Text(
//                    "${o.subtitle} • ${o.timeFormatted}",
//                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis
//                )
//            },
//            trailingContent = {
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.spacedBy(8.dp)
//                ) {
//                    if (o.isDraft) {
//                        AssistChip(
//                            onClick = { onSaveClick(o.id) },
//                            label = { Text("Save") },
//                            enabled = true
//                        )
//                    } else {
//                        Text(o.adjustedAmount.toString())
//                    }
//                    AssistChip(
//                        onClick = { onDeleteClick(o.id) },
//                        label = { Text("Delete") },
//                        enabled = true
//                    )
//                }
//
//            }
//        )
//    }
//}
//
//@Composable
//private fun EmptyOrders() {
//    Box(
//        Modifier
//            .fillMaxWidth()
//            .padding(24.dp), contentAlignment = Alignment.Center
//    ) {
//        Text(
//            "No orders yet for this customer. Tap “New Sale”.",
//            style = MaterialTheme.typography.bodyLarge
//        )
//    }
//}
//
//@Composable
//private fun ErrorBanner(message: String, onDismiss: () -> Unit) {
//    Surface(tonalElevation = 2.dp, color = MaterialTheme.colorScheme.errorContainer) {
//        Row(
//            Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 16.dp, vertical = 12.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Text(
//                message,
//                color = MaterialTheme.colorScheme.onErrorContainer,
//                modifier = Modifier.weight(1f)
//            )
//            TextButton(onClick = onDismiss) { Text("Dismiss") }
//        }
//    }
//}
//
//@Preview(showBackground = true)
//@Composable
//fun PreviewHeaderCard() {
//    HeaderCard(
//        header = CustomerHeaderUi(
//            displayName = "Jane Doe",
//            phoneFormatted = "+1 555-1234",
//            totalOrders = 12,
//            lifetimeValueFormatted = "$1,200.00",
//            lastVisitFormatted = "Apr 10, 2024",
//            id = "test"
//        )
//    )
//}
//
//@Preview(showBackground = true)
//@Composable
//fun PreviewOrdersList() {
//    val sampleOrders = listOf(
//        OrderSummaryUi(
//            id = "1",
//            invoiceShort = "INV-001",
//            subtitle = "First order",
//            timeFormatted = "10:00 AM",
//            isDraft = false,
//            advanceTotal = 100,
//            remainingBalance = 50,
//            totalAmount = 150,
//            adjustedAmount = 0
//        ),
//        OrderSummaryUi(
//            id = "2",
//            invoiceShort = "INV-002",
//            subtitle = "Second order",
//            timeFormatted = "11:30 AM",
//            isDraft = true,
//            advanceTotal = 100,
//            remainingBalance = 50,
//            totalAmount = 150,
//            adjustedAmount = 0
//        )
//    )
//    OrdersList(
//        ordersByDay = mapOf("Today" to sampleOrders),
//        onOrderClick = {}
//    )
//}
//
