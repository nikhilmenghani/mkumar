package com.mkumar.ui.screens

import android.widget.Toast
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.mkumar.MainActivity
import com.mkumar.common.constant.AppConstants.getAppDownloadUrl
import com.mkumar.common.constant.AppConstants.getExternalStorageDir
import com.mkumar.common.extension.navigateWithState
import com.mkumar.common.manager.PackageManager.getCurrentVersion
import com.mkumar.common.manager.PackageManager.installApk
import com.mkumar.data.CustomerFormState
import com.mkumar.network.VersionFetcher.fetchLatestVersion
import com.mkumar.ui.components.bottomsheets.ShortBottomSheet
import com.mkumar.ui.components.cards.CustomerInfoCard
import com.mkumar.ui.components.cards.CustomerListCard2
import com.mkumar.ui.components.dialogs.ConfirmActionDialog
import com.mkumar.ui.components.fabs.StandardFab
import com.mkumar.ui.navigation.Material3BottomNavigationBar
import com.mkumar.ui.navigation.Routes
import com.mkumar.ui.navigation.Screens
import com.mkumar.viewmodel.CustomerViewModel
import com.mkumar.worker.DownloadWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
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

    // Search panel state (new)
    var isSearchPanelOpen by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val customers by vm.customersUi.collectAsStateWithLifecycle()
    val currentCustomerId by vm.currentCustomerId.collectAsStateWithLifecycle()

    val openFormsForCurrentFlow = remember(currentCustomerId) { MutableStateFlow(emptySet<String>()) }

    var sheetMode by remember { mutableStateOf(CustomerSheetMode.Add) }
    var showCustomerSheet by remember { mutableStateOf(false) }
    var editingCustomerId by remember { mutableStateOf<String?>(null) }

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    val haptic = LocalHapticFeedback.current
    var deleteTarget by remember { mutableStateOf<CustomerFormState?>(null) }

    val canSubmit by remember(name, phone) {
        val digits = phone.count { it.isDigit() }
        mutableStateOf(name.isNotBlank() && digits == 10)
    }
    var showFabs by remember { mutableStateOf(true) }

    LaunchedEffect(currentCustomerId) {
        vm.openForms.collect { map ->
            openFormsForCurrentFlow.value = map[currentCustomerId].orEmpty()
        }
    }

    LaunchedEffect(Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            latestVersion = fetchLatestVersion()
            isLatestVersion = (currentVersion == latestVersion) || (latestVersion == "Unknown")
        }
    }

    val density = LocalDensity.current
    var fabBlockHeight by remember { mutableStateOf(0.dp) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "M Kumar")
                        Text(
                            text = "v$currentVersion",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { context.restartActivity() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = {
                        navController.navigateWithState(route = Screens.Settings.name)
                    }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets(0.dp), // avoids keyboard pushing whole layout up
        floatingActionButton = {
            AnimatedVisibility(visible = showFabs) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.onGloballyPositioned { coords ->
                        fabBlockHeight = with(density) { coords.size.height.toDp() }
                    }
                ) {
                    StandardFab(
                        text = "",
                        icon = {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        onClick = {
                            sheetMode = CustomerSheetMode.Add
                            editingCustomerId = null
                            name = ""
                            phone = ""
                            showCustomerSheet = true
                        },
                    )

                    // Search FAB now opens search panel instead of navigating
                    StandardFab(
                        text = "",
                        icon = {
                            Icon(
                                Icons.Default.PersonSearch,
                                contentDescription = "Search",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isSearchPanelOpen = true
                            showFabs = false     // hide FABs
                        },
                    )

                    if (!isLatestVersion) {
                        StandardFab(
                            text = "",
                            icon = {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "Update",
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            loading = isDownloading,
                            onClick = {
                                isDownloading = true
                                val downloadUrl = getAppDownloadUrl(latestVersion)
                                val destFilePath = "${getExternalStorageDir()}/Download/MKumar.apk"

                                val inputData = workDataOf(
                                    DownloadWorker.DOWNLOAD_URL_KEY to downloadUrl,
                                    DownloadWorker.DEST_FILE_PATH_KEY to destFilePath,
                                    DownloadWorker.DOWNLOAD_TYPE_KEY to DownloadWorker.DOWNLOAD_TYPE_APK
                                )

                                val downloadRequest =
                                    OneTimeWorkRequestBuilder<DownloadWorker>()
                                        .setInputData(inputData)
                                        .setConstraints(
                                            Constraints.Builder()
                                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                                .build()
                                        )
                                        .build()

                                workManager.enqueue(downloadRequest)
                                workManager.getWorkInfoByIdLiveData(downloadRequest.id)
                                    .observeForever { info ->
                                        if (info?.state == WorkInfo.State.SUCCEEDED) {
                                            isDownloading = false
                                            if (context.packageManager.canRequestPackageInstalls()) {
                                                installApk(context, destFilePath)
                                            }
                                        } else if (info?.state == WorkInfo.State.FAILED) {
                                            isDownloading = false
                                            Toast.makeText(
                                                context,
                                                "Failed to download update",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
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
            // Main content
            Column {
                CustomerList(
                    customers = customers,
                    onClick = { customer ->
                        vm.selectCustomer(customer.id)
                        navController.navigate(Routes.customerDetail(customer.id))
                    },
                    onDelete = { customer -> deleteTarget = customer },
                    onEdit = { customer ->
                        sheetMode = CustomerSheetMode.Edit
                        editingCustomerId = customer.id
                        name = customer.name
                        phone = customer.phone
                        showCustomerSheet = true
                    },
                    extraBottomPadding = fabBlockHeight + 16.dp
                )
            }

            // Search panel at bottom, like Apps tab
            AnimatedVisibility(
                visible = isSearchPanelOpen,
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                modifier = Modifier.alpha(0.75f),
                                text = "Search customers"
                            )
                        },
                        leadingIcon = {
                            IconButton(onClick = {
                                isSearchPanelOpen = false
                                showFabs = true
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = null
                                )
                            }
                        },
                        trailingIcon = {
                            AnimatedVisibility(visible = searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        if (isSearching) {
                                            isSearching = false
                                        } else {
                                            searchQuery = ""
                                            isSearching = false
//                                            vm.search("") // clear search
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isSearching) Icons.Rounded.Pause else Icons.Rounded.Cancel,
                                        contentDescription = null
                                    )
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                isSearching = true
//                                vm.search(searchQuery)
                            }
                        ),
                        colors = TextFieldDefaults.colors()
                    )
                }
            }

            // Center loading indicator when searching
            AnimatedVisibility(
                visible = isSearching,
                modifier = Modifier.align(Alignment.Center)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    if (showCustomerSheet) {
        ShortBottomSheet(
            title = if (sheetMode == CustomerSheetMode.Add) "Add Customer" else "Edit Customer",
            showTitle = false,
            sheetContent = {
                CustomerInfoCard(
                    title = if (sheetMode == CustomerSheetMode.Add)
                        "Add Customer Information"
                    else
                        "Edit Customer Information",
                    name = name,
                    phone = phone,
                    onNameChange = { name = it },
                    onPhoneChange = { phone = it },
                    onSubmit = {
                        if (!canSubmit) {
                            // optional feedback:
                            // scope.launch { snackbarHostState.showSnackbar("Enter at least 9 digits") }
                            return@CustomerInfoCard
                        }

                        if (sheetMode == CustomerSheetMode.Add) {
                            val customerId =
                                vm.createOrUpdateCustomerCard(name.trim(), phone.trim())
                            vm.selectCustomer(customerId)
                            navController.navigate(Routes.customerDetail(customerId))
                        } else {
                            editingCustomerId?.let {
                                vm.updateCustomer(it, name.trim(), phone.trim())
                            }
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
                    val customerId =
                        vm.createOrUpdateCustomerCard(name.trim(), phone.trim())
                    vm.selectCustomer(customerId)
                    navController.navigate(Routes.customerDetail(customerId))
                } else {
                    editingCustomerId?.let {
                        vm.updateCustomer(it, name.trim(), phone.trim())
                    }
                }
                showCustomerSheet = false
            }
        )
    }

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

@OptIn(ExperimentalMaterial3Api::class)
@Preview(
    name = "Nord 3 Portrait",
    showSystemUi = true,
    device = "spec:width=1240px,height=2772px,dpi=450"
)
@PreviewFontScale
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        val navController = rememberNavController()
        Scaffold(
            topBar = { TopAppBar(title = { Text("M Kumar") }) },
            bottomBar = { Material3BottomNavigationBar(navController) },
            contentWindowInsets = WindowInsets(0.dp)
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                CustomerList(
                    customers = listOf(
                        CustomerFormState(id = "1", name = "John Doe", phone = "9876543210"),
                        CustomerFormState(id = "2", name = "Jane Smith", phone = "8765432109"),
                        CustomerFormState(id = "3", name = "Bob Wilson", phone = "7654321098")
                    ),
                    onClick = {},
                    onDelete = {},
                    onEdit = {},
                    extraBottomPadding = 0.dp
                )
            }
        }
    }
}
