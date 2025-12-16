package com.mkumar.ui.screens.search

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.Intent
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.mkumar.MainActivity
import com.mkumar.model.CustomerDetailsEffect
import com.mkumar.model.CustomerSheetMode
import com.mkumar.model.OrderWithCustomerInfo
import com.mkumar.model.SearchBy
import com.mkumar.model.SearchMode
import com.mkumar.model.SearchType
import com.mkumar.model.UiCustomerMini
import com.mkumar.ui.components.bottomsheets.ShortBottomSheet
import com.mkumar.ui.components.cards.CustomerInfoCard
import com.mkumar.ui.components.dialogs.ConfirmActionDialog
import com.mkumar.ui.navigation.Routes
import com.mkumar.ui.screens.RecentCustomerCard
import com.mkumar.ui.screens.RecentCustomersList
import com.mkumar.ui.screens.RecentOrdersList
import com.mkumar.viewmodel.SearchViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

// ================================================================
// MAIN SCREEN
// ================================================================
@SuppressLint("UnrememberedMutableState")
@Composable
fun SearchScreen(
    navController: NavHostController,
    vm: SearchViewModel = hiltViewModel(),
    onBack: () -> Unit = { navController.popBackStack() },
    openCustomer: (String) -> Unit = { id -> navController.navigate("CustomerDetail/$id") }
) {
    val ui by vm.ui.collectAsState()
    var showAdvancedOptions by remember { mutableStateOf(false) }

    // -------------------------------
    // ADD CUSTOMER BOTTOM-SHEET STATE
    // -------------------------------
    var showCustomerSheet by remember { mutableStateOf(false) }
    var addName by remember { mutableStateOf("") }
    var addPhone by remember { mutableStateOf("") }

    val canSubmit by derivedStateOf {
        addName.isNotBlank() && addPhone.length == 10
    }

    var sheetMode by remember { mutableStateOf(CustomerSheetMode.Add) }
    var editingCustomerId by remember { mutableStateOf<String?>(null) }

    // -------------------------------
    // Auto-focus Search Box
    // -------------------------------
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalActivity.current as MainActivity
    val (pendingDeleteOrderId, setPendingDeleteOrderId) = remember { mutableStateOf<String?>(null) }
    var deleteCustomer by remember { mutableStateOf<UiCustomerMini?>(null) }

    LaunchedEffect(Unit) {
        delay(100)
        focusRequester.requestFocus()
        keyboard?.show()
        vm.effects.collectLatest { effect ->
            when (effect) {
                is CustomerDetailsEffect.ShowMessage ->
                    snackbarHostState.showSnackbar(effect.message)

                is CustomerDetailsEffect.ViewInvoice -> {
                    val uri = effect.uri
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "application/pdf")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    val chooser = Intent.createChooser(intent, "Open invoice")
                    runCatching { context.startActivity(chooser) }
                        .onFailure { _ ->
                            val hint = vm.humanReadableInvoiceLocation(effect.orderId, effect.invoiceNumber)
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
                        clipData = ClipData.newRawUri("invoice", effect.uri)
                    }
                    runCatching { context.startActivity(Intent.createChooser(send, "Share invoice")) }
                        .onFailure {
                            snackbarHostState.showSnackbar(
                                "No app to share PDF. File is in Files > Downloads > Documents > MKumar > Invoices"
                            )
                        }
                }

                else -> {}
            }
        }
    }

    val navBackStackEntry = navController.currentBackStackEntryAsState()
    LaunchedEffect(navBackStackEntry.value) {
        // Call a function in your ViewModel to refresh recents
        vm.loadRecent()
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .imePadding()
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        // HEADER
        SearchHeader(
            query = ui.query,
            isSearching = ui.isSearching,
            onBackClick = onBack,
            onQueryChange = {
                vm.updateQuery(it)
                showAdvancedOptions = false
            },
            onStopClick = vm::stopSearch,
            onAdvancedToggle = { showAdvancedOptions = !showAdvancedOptions },
            focusRequester = focusRequester,
            searchBy = ui.searchBy
        )

        // ADVANCED OPTIONS
        AnimatedVisibility(
            visible = showAdvancedOptions,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            SearchAdvancedOptions(
                mode = ui.mode,
                searchBy = ui.searchBy,
                searchType = ui.searchType,
                onModeChange = vm::updateMode,
                onSearchByChange = vm::updateSearchBy,
                onSearchTypeChange = vm::updateSearchType
            )
        }

        // SEARCH PROGRESS
        AnimatedVisibility(
            visible = ui.isSearching,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            SearchProgress()
        }

        if (ui.searchType == SearchType.ORDERS) {
            SearchOrderResultsSection(
                orderResults = ui.orderResults,
                invoicePrefix = vm.getInvoicePrefix(),
                isSearching = ui.isSearching,
                query = ui.query,
                onClear = vm::clearResults,
                onOrderClick = { orderId, customerId ->
                    navController.navigate(Routes.orderEditor(customerId, orderId))
                },
                onInvoiceClick = { orderId, invoiceNumber ->
                    vm.viewInvoice(orderId, invoiceNumber.toString())
                },
                onShareClick = { orderId, invoiceNumber ->
                    vm.shareInvoice(orderId, invoiceNumber.toString())
                },
                onDeleteClick = { orderId ->
                    setPendingDeleteOrderId(orderId)
                },
                onOpenCustomer = { customerId ->
                    navController.navigate(Routes.customerDetail(customerId))
                }
            )
        } else {
            SearchCustomerResultsSection(
                results = ui.results,
                recent = ui.recent,
                searchBy = ui.searchBy,
                isSearching = ui.isSearching,
                query = ui.query,
                onClear = vm::clearResults,
                onAddCustomer = { prefillName, prefillPhone ->
                    addName = prefillName ?: ""
                    addPhone = prefillPhone ?: ""
                    showCustomerSheet = true
                },
                openCustomer = openCustomer,
                onEdit = { customer ->
                    editingCustomerId = customer.id
                    addName = customer.name
                    addPhone = customer.phone
                    sheetMode = CustomerSheetMode.Edit
                    showCustomerSheet = true
                },
                onDelete = { customer ->
                    deleteCustomer = customer
                },
            )
        }
        // RESULTS

    }

    if (pendingDeleteOrderId != null) {
        ConfirmActionDialog(
            title = "Delete Order",
            message = "This action cannot be undone. Delete this order?",
            confirmLabel = "Delete",
            dismissLabel = "Cancel",
            highlightConfirmAsDestructive = true,
            onConfirm = {
                vm.deleteOrder(pendingDeleteOrderId)
                setPendingDeleteOrderId(null)
                vm.triggerSearch()
            },
            onDismiss = { setPendingDeleteOrderId(null) }
        )
    }

    if (deleteCustomer != null) {
        ConfirmActionDialog(
            title = "Delete Customer",
            message = "This action cannot be undone. Delete this customer?",
            confirmLabel = "Delete",
            dismissLabel = "Cancel",
            highlightConfirmAsDestructive = true,
            onConfirm = {
                vm.removeCustomer(deleteCustomer!!.id)
                vm.triggerSearch()
                deleteCustomer = null
            },
            onDismiss = { deleteCustomer = null }
        )
    }

    // ===========================================================
    // ADD CUSTOMER BOTTOM SHEET
    // ===========================================================
    if (showCustomerSheet) {
        ShortBottomSheet(
            title = if (sheetMode == CustomerSheetMode.Add) "Add Customer" else "Edit Customer",
            showTitle = false,
            sheetContent = {
                CustomerInfoCard(
                    title = if (sheetMode == CustomerSheetMode.Add) "Add Customer Information" else "Edit Customer Information",
                    name = addName,
                    phone = addPhone,
                    onNameChange = { addName = it },
                    onPhoneChange = { addPhone = it },

                    onSubmit = {
                        if (!canSubmit) {
                            // optional feedback:
                            // scope.launch { snackbarHostState.showSnackbar("Enter at least 9 digits") }
                            return@CustomerInfoCard
                        }
                        if (sheetMode == CustomerSheetMode.Add) {
                            val customerId = vm.createOrUpdateCustomerCard(addName.trim(), addPhone.trim())
                            navController.navigate(Routes.customerDetail(customerId))
                        } else {
                            editingCustomerId?.let { vm.updateCustomer(it, addName.trim(), addPhone.trim()) }
                        }
                        showCustomerSheet = false
                    }
                )
            },
            onDismiss = { showCustomerSheet = false },

            showDismiss = true,
            showDone = canSubmit,

            onDoneClick = {
                if (!canSubmit) return@ShortBottomSheet
                if (sheetMode == CustomerSheetMode.Add) {
                    val customerId = vm.createOrUpdateCustomerCard(addName.trim(), addPhone.trim())
                    navController.navigate(Routes.customerDetail(customerId))
                } else {
                    editingCustomerId?.let { vm.updateCustomer(it, addName.trim(), addPhone.trim()) }
                }
                showCustomerSheet = false
            }
        )
    }
}

// ================================================================
// HEADER
// ================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchHeader(
    query: String,
    isSearching: Boolean,
    onBackClick: () -> Unit,
    onQueryChange: (String) -> Unit,
    onStopClick: () -> Unit,
    onAdvancedToggle: () -> Unit,
    focusRequester: FocusRequester,
    searchBy: SearchBy
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerLow),
            shape = CircleShape,
            placeholder = {
                Text(
                    text = "Search name or phone…",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            },
            leadingIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = null
                    )
                }
            },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    when {
                        isSearching -> {
                            IconButton(onClick = onStopClick) {
                                Icon(Icons.Rounded.Stop, null)
                            }
                        }
                        query.isNotEmpty() -> {
                            IconButton(onClick = { onQueryChange("") }) {
                                Icon(Icons.Filled.Close, null)
                            }
                        }
                    }

                    IconButton(onClick = onAdvancedToggle) {
                        Icon(Icons.Rounded.Tune, null)
                    }
                }
            },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search,
                capitalization = KeyboardCapitalization.Words,
                keyboardType = when (searchBy) {
                    SearchBy.PHONE -> KeyboardType.Number
                    SearchBy.INVOICE -> KeyboardType.Number
                    else -> KeyboardType.Text
                }
            ),
            keyboardActions = KeyboardActions(onSearch = {})
        )
    }
}

// ================================================================
// ADVANCED OPTIONS
// ================================================================
@Composable
private fun SearchAdvancedOptions(
    mode: SearchMode,
    searchBy: SearchBy,
    searchType: SearchType,
    onModeChange: (SearchMode) -> Unit,
    onSearchByChange: (SearchBy) -> Unit,
    onSearchTypeChange: (SearchType) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.surfaceContainerHigh,
                        MaterialTheme.colorScheme.surfaceContainerLow
                    )
                )
            )
            .padding(20.dp)
    ) {
        // ------------------------------------------------------------
        // Header
        // ------------------------------------------------------------
        Text(
            "Advanced Options",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // ------------------------------------------------------------
        // Mode Section
        // ------------------------------------------------------------
        SectionHeader("Search speed")
        Spacer(Modifier.height(8.dp))

        Row {
            OptionChip(
                label = "Fast",
                selected = mode == SearchMode.QUICK,
                onClick = { onModeChange(SearchMode.QUICK) }
            )
            Spacer(Modifier.width(8.dp))
            OptionChip(
                label = "Flexible",
                selected = mode == SearchMode.FLEXIBLE,
                onClick = { onModeChange(SearchMode.FLEXIBLE) }
            )
        }

        Spacer(Modifier.height(20.dp))

        // ------------------------------------------------------------
        // Search By Section
        // ------------------------------------------------------------
        SectionHeader("Search by")
        Spacer(Modifier.height(8.dp))

        Row {
            SearchByChip("Name", SearchBy.NAME, searchBy) {
                onSearchByChange(SearchBy.NAME)
                if (searchType == SearchType.ORDERS) {
                    // auto-reset if switching to name/phone
                    onSearchTypeChange(SearchType.CUSTOMERS)
                }
            }
            Spacer(Modifier.width(8.dp))

            SearchByChip("Phone", SearchBy.PHONE, searchBy) {
                onSearchByChange(SearchBy.PHONE)
                if (searchType == SearchType.ORDERS) {
                    onSearchTypeChange(SearchType.CUSTOMERS)
                }
            }
            Spacer(Modifier.width(8.dp))

            SearchByChip("Invoice", SearchBy.INVOICE, searchBy) {
                onSearchByChange(SearchBy.INVOICE)
                // FORCE orders when invoice search
                onSearchTypeChange(SearchType.ORDERS)
            }
        }

        Spacer(Modifier.height(20.dp))

        // ------------------------------------------------------------
        // Return results Section (Customers / Orders)
        // ------------------------------------------------------------
        SectionHeader("Return results")
        Spacer(Modifier.height(8.dp))

        val isForcedOrder = searchBy == SearchBy.INVOICE

        Row {
            ReturnTypeChip(
                label = "Customers",
                selected = searchType == SearchType.CUSTOMERS,
                enabled = !isForcedOrder,
                onClick = { onSearchTypeChange(SearchType.CUSTOMERS) }
            )
            Spacer(Modifier.width(8.dp))

            ReturnTypeChip(
                label = "Orders",
                selected = searchType == SearchType.ORDERS,
                enabled = searchBy == SearchBy.INVOICE, // always allowed
                onClick = {
                    onSearchTypeChange(SearchType.ORDERS)
                    onSearchByChange(SearchBy.INVOICE)
                }
            )
        }

        if (isForcedOrder) {
            Text(
                "Invoice search always returns orders.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun OptionChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        color = if (selected)
            MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceContainer
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            color = if (selected)
                MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SearchByChip(
    label: String,
    value: SearchBy,
    selected: SearchBy,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        color = if (selected == value)
            MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceContainer
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            color = if (selected == value)
                MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ReturnTypeChip(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val bg = when {
        !enabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        selected -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceContainer
    }

    val fg = when {
        !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        selected -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = enabled) { onClick() },
        color = bg
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            color = fg
        )
    }
}

// ================================================================
// SEARCH PROGRESS
// ================================================================
@Composable
private fun SearchProgress() {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            "Searching…",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(8.dp))

        LinearProgressIndicator(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun SearchOrderResultsSection(
    orderResults: List<OrderWithCustomerInfo>,
    invoicePrefix: String,
    isSearching: Boolean,
    query: String,
    onClear: () -> Unit,
    onOrderClick: (orderId: String, customerId: String) -> Unit,
    onInvoiceClick: (orderId: String, invoiceNumber: Long) -> Unit,
    onShareClick: (orderId: String, invoiceNumber: Long) -> Unit,
    onDeleteClick: (orderId: String) -> Unit,
    onOpenCustomer: (customerId: String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header ----------------------------------------------
        Row(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Orders (${orderResults.size})",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.weight(1f))

            if (query.isNotBlank() && orderResults.isNotEmpty()) {
                TextButton(onClick = onClear) {
                    Text("Clear")
                }
            }
        }

        // Empty State ------------------------------------------
        if (orderResults.isEmpty() && !isSearching) {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No matching orders found.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return
        }
        Column(Modifier.padding(14.dp)) {
            RecentOrdersList(
                orders = orderResults,
                invoicePrefix = invoicePrefix,
                onOrderClick = onOrderClick,
                onInvoiceClick = onInvoiceClick,
                onShareClick = onShareClick,
                onDeleteClick = onDeleteClick,
                onOpenCustomer = onOpenCustomer
            )
        }
    }
}

// ================================================================
// RESULTS + RECENT + ADD CUSTOMER BUTTON
// ================================================================
@Composable
private fun SearchCustomerResultsSection(
    results: List<UiCustomerMini>,
    recent: List<UiCustomerMini>,
    searchBy: SearchBy,
    isSearching: Boolean,
    query: String,
    onClear: () -> Unit,
    onAddCustomer: (prefillName: String?, prefillPhone: String?) -> Unit,
    openCustomer: (String) -> Unit,
    onEdit: (customer: UiCustomerMini) -> Unit,
    onDelete: (customer: UiCustomerMini) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {

        val headerTitle =
            if (query.isBlank()) "Recently Added Customers"
            else "Results (${results.size})"

        Row(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                headerTitle,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.weight(1f))

            if (query.isNotBlank() && results.isNotEmpty()) {
                TextButton(onClick = onClear) {
                    Text("Clear")
                }
            }
        }

        // ---------------------------------------------------------
        // NO QUERY → Show Recent
        // ---------------------------------------------------------
        if (query.isBlank()) {
            if (recent.isEmpty()) {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No recent customers.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                RecentCustomersList(
                    customers = recent,
                    onCustomerClick = { customer ->
                        openCustomer(customer.id)
                    },
                    onEdit = { customer ->
                        onEdit(customer)
                    },
                    onDelete = { customer ->
                        onDelete(customer)
                    }
                )
            }
            return
        }

        // ---------------------------------------------------------
        // NO RESULTS → Add Customer Button
        // ---------------------------------------------------------
        if (results.isEmpty() && !isSearching) {

            // Don't allow adding customers while searching by Invoice
            if (searchBy == SearchBy.INVOICE) {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No matches found.")
                }
                return
            }

            NoResultsAddCustomer(
                query = query,
                searchBy = searchBy,
                openAddCustomerSheet = onAddCustomer
            )
            return
        }

        // ---------------------------------------------------------
        // RESULTS LIST
        // ---------------------------------------------------------
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            itemsIndexed(results, key = { _, it -> it.id }) { _, c ->
                RecentCustomerCard(
                    customer = c,
                    onClick = { openCustomer(c.id) },
                    onEdit = { onEdit(c) },
                    onDelete = { onDelete(c) }
                )
            }
        }
    }
}

// ================================================================
// ADD CUSTOMER BUTTON (NO RESULTS STATE)
// ================================================================
@Composable
private fun NoResultsAddCustomer(
    query: String,
    searchBy: SearchBy,
    openAddCustomerSheet: (String?, String?) -> Unit
) {
    val prefillName: String?
    val prefillPhone: String?

    when (searchBy) {
        SearchBy.NAME -> {
            prefillName = query
            prefillPhone = null
        }
        SearchBy.PHONE -> {
            prefillName = null
            prefillPhone = query
        }
        SearchBy.INVOICE -> {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No matches found.")
            }
            return
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "No matching customers found.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(24.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clickable {
                    openAddCustomerSheet(prefillName, prefillPhone)
                },
            color = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Add customer",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ================================================================
// RESULT ITEM
// ================================================================
@Composable
fun SearchResultItem(c: UiCustomerMini, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                c.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                c.phone,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
