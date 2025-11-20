package com.mkumar.ui.screens

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.work.WorkManager
import com.mkumar.MainActivity
import com.mkumar.common.extension.navigateWithState
import com.mkumar.common.manager.PackageManager.getCurrentVersion
import com.mkumar.data.CustomerFormState
import com.mkumar.network.VersionFetcher.fetchLatestVersion
import com.mkumar.ui.components.cards.CustomerListCard2
import com.mkumar.ui.components.dialogs.ConfirmActionDialog
import com.mkumar.ui.components.fabs.StandardFab
import com.mkumar.ui.navigation.Routes
import com.mkumar.ui.navigation.Screens
import com.mkumar.viewmodel.CustomerViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

enum class CustomerSheetMode { Add, Edit }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController, vm: CustomerViewModel) {

    val context = LocalActivity.current as MainActivity
    val workManager = WorkManager.getInstance(context)

    val currentVersion by remember { mutableStateOf(getCurrentVersion(context)) }
    var latestVersion by remember { mutableStateOf(currentVersion) }
    var isLatestVersion by remember { mutableStateOf(true) }
    var isDownloading by remember { mutableStateOf(false) }

    // SEARCH STATE
    var isSearchPanelOpen by remember { mutableStateOf(false) }
    var showFabs by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val customers by vm.customersUi.collectAsStateWithLifecycle()
    val currentCustomerId by vm.currentCustomerId.collectAsStateWithLifecycle()

    val haptic = LocalHapticFeedback.current

    var deleteTarget by remember { mutableStateOf<CustomerFormState?>(null) }
    val density = LocalDensity.current
    var fabBlockHeight by remember { mutableStateOf(0.dp) }

    // Auto-focus when search panel opens
    LaunchedEffect(isSearchPanelOpen) {
        if (isSearchPanelOpen) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    // Fetch version
    LaunchedEffect(Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            latestVersion = fetchLatestVersion()
            isLatestVersion = (currentVersion == latestVersion) || (latestVersion == "Unknown")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("M Kumar")
                        Text(
                            "v$currentVersion",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { context.restartActivity() }) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                    IconButton(onClick = {
                        navController.navigateWithState(route = Screens.Settings.name)
                    }) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                }
            )
        },

        // DO NOT adjust main UI for keyboard (AppsTab also does this)
        contentWindowInsets = WindowInsets(0.dp),

        floatingActionButton = {
            AnimatedVisibility(showFabs) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.onGloballyPositioned { coords ->
                        fabBlockHeight = with(density) { coords.size.height.toDp() }
                    }
                ) {
                    // Add customer
                    StandardFab(
                        text = "",
                        icon = { Icon(Icons.Default.Add, null, modifier = Modifier.size(24.dp)) },
                        onClick = {
                            // your add logic unchanged...
                        }
                    )

                    // Search FAB — opens panel
                    StandardFab(
                        text = "",
                        icon = { Icon(Icons.Default.PersonSearch, null, modifier = Modifier.size(24.dp)) },
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isSearchPanelOpen = true
                            showFabs = false
                        }
                    )

                    // Update FAB
                    if (!isLatestVersion) {
                        StandardFab(
                            text = "",
                            icon = { Icon(Icons.Default.Refresh, "Update", modifier = Modifier.size(24.dp)) },
                            loading = isDownloading,
                            onClick = {
                                // update worker logic unchanged...
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            Column {
                CustomerList(
                    customers = customers,
                    onClick = { customer ->
                        vm.selectCustomer(customer.id)
                        navController.navigate(Routes.customerDetail(customer.id))
                    },
                    onDelete = { customer -> deleteTarget = customer },
                    onEdit = { customer ->
                        vm.selectCustomer(customer.id)
                        // Your edit logic...
                    },
                    extraBottomPadding = fabBlockHeight + 16.dp
                )
            }
        }
    }

    // DELETE CONFIRMATION
    if (deleteTarget != null) {
        ConfirmActionDialog(
            title = "Delete Customer",
            message = "This action cannot be undone. Delete this customer?",
            confirmLabel = "Delete",
            dismissLabel = "Cancel",
            highlightConfirmAsDestructive = true,
            onConfirm = {
                vm.removeCustomer(deleteTarget!!.id)
                deleteTarget = null
            },
            onDismiss = { deleteTarget = null }
        )
    }

    // ⭐ SUPER IMPORTANT ⭐
    // This is your AppsTab-style bottom anchored search input.
    if (isSearchPanelOpen) {
        Dialog(
            onDismissRequest = {
                isSearchPanelOpen = false
                showFabs = true
                keyboardController?.hide()
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp) // fixed gap above keyboard
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text("Search customers", modifier = Modifier.alpha(0.75f))
                            },

                            leadingIcon = {
                                IconButton(
                                    onClick = {
                                        isSearchPanelOpen = false
                                        showFabs = true
                                        keyboardController?.hide()
                                    }
                                ) {
                                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, null)
                                }
                            },

                            trailingIcon = {
                                AnimatedVisibility(searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = {
                                            if (isSearching) {
                                                isSearching = false
                                            } else {
                                                searchQuery = ""
                                                isSearching = false
//                                                vm.search("")
                                            }
                                        }
                                    ) {
                                        Icon(
                                            if (isSearching) Icons.Rounded.Pause else Icons.Rounded.Cancel,
                                            null
                                        )
                                    }
                                }
                            },

                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    isSearching = true
//                                    vm.search(searchQuery)
                                }
                            ),

                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = AlertDialogDefaults.containerColor,
                                unfocusedContainerColor = AlertDialogDefaults.containerColor,
                                disabledContainerColor = AlertDialogDefaults.containerColor,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun CustomerList(
    customers: List<CustomerFormState>,
    onClick: (CustomerFormState) -> Unit = {},
    onDelete: (CustomerFormState) -> Unit = {},
    onEdit: (CustomerFormState) -> Unit = {},
    extraBottomPadding: Dp = 0.dp
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = 8.dp,
            top = 8.dp,
            end = 8.dp,
            bottom = 8.dp + extraBottomPadding
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(customers, key = { it.id }) { customer ->
            CustomerListCard2(
                customer = customer,
                onClick = onClick,
                onEdit = onEdit,
                onDelete = onDelete
            )
        }
    }
}
