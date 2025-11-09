package com.mkumar.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
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
import com.mkumar.ui.components.fabs.StandardFab
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

    // UI flags
    var showAddCustomerSheet by remember { mutableStateOf(false) }
    var showJsonDialog by remember { mutableStateOf(false) }
    var jsonPreview by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // ViewModel state
    val customers by vm.customersUi.collectAsStateWithLifecycle()
    val currentCustomerId by vm.currentCustomerId.collectAsStateWithLifecycle()
    val openFormsForCurrentFlow = remember(currentCustomerId) { MutableStateFlow(emptySet<String>()) }
    var sheetMode by remember { mutableStateOf(CustomerSheetMode.Add) }
    var showCustomerSheet by remember { mutableStateOf(false) }
    var editingCustomerId by remember { mutableStateOf<String?>(null) }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    LaunchedEffect(currentCustomerId) {
        // Whenever the VM's openForms map changes, push only the current customer's set
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "M Kumar") },
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
        floatingActionButton = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                StandardFab(
                    text = "Add a new Customer",
                    icon = { Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(24.dp)) },
                    onClick = {
                        sheetMode = CustomerSheetMode.Add
                        editingCustomerId = null
                        name = ""
                        phone = ""
                        showCustomerSheet = true
                    },
                )
                if (!isLatestVersion) {
                    StandardFab(
                        text = "MKumar v$latestVersion Available",
                        icon = { Icon(Icons.Default.Refresh, contentDescription = "Update", modifier = Modifier.size(24.dp)) },
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
                            val downloadRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
                                .setInputData(inputData)
                                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                                .build()
                            workManager.enqueue(downloadRequest)
                            workManager.getWorkInfoByIdLiveData(downloadRequest.id).observeForever { info ->
                                if (info?.state == WorkInfo.State.SUCCEEDED) {
                                    isDownloading = false
                                    if (context.packageManager.canRequestPackageInstalls()) {
                                        installApk(context, destFilePath)
                                    }
                                } else if (info?.state == WorkInfo.State.FAILED) {
                                    isDownloading = false
                                    Toast.makeText(context, "Failed to download update", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            CustomerList(
                customers = customers,
                onClick = { customer ->
                    vm.selectCustomer(customer.id)
                    navController.navigate(Routes.customerDetail(customer.id))
                },
                // We still keep direct update callback for other entry points if you want:
                onUpdateCustomer = { id, n, p -> vm.updateCustomer(id, n, p) },
                onDelete = { customer -> vm.removeCustomer(customer.id) },
                // NEW: Provide onEdit to open the same ShortBottomSheet prefilled
                onEdit = { customer ->
                    sheetMode = CustomerSheetMode.Edit
                    editingCustomerId = customer.id
                    name = customer.name
                    phone = customer.phone
                    showCustomerSheet = true
                }
            )
        }
    }

    // --- Add Customer Sheet (local inputs; VM is multi-customer now) ---
    if (showCustomerSheet) {
        ShortBottomSheet(
            title = if (sheetMode == CustomerSheetMode.Add) "Add Customer" else "Edit Customer",
            showTitle = false,
            sheetContent = {
                CustomerInfoCard(
                    name = name,
                    phone = phone,
                    onNameChange = { name = it },
                    onPhoneChange = { phone = it }
                )
            },
            onDismiss = { showCustomerSheet = false },
            showDismiss = true,
            showDone = true,
            onDoneClick = {
                if (name.isNotBlank()) {
                    if (sheetMode == CustomerSheetMode.Add) {
                        vm.createOrUpdateCustomerCard(name.trim(), phone.trim())
                    } else {
                        editingCustomerId?.let { vm.updateCustomer(it, name.trim(), phone.trim()) }
                    }
                    showCustomerSheet = false
                }
            }
        )
    }

    if (showJsonDialog) {
        AlertDialog(
            onDismissRequest = { showJsonDialog = false },
            title = { Text("Form JSON") },
            text = {
                val scroll = rememberScrollState()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp, max = 420.dp)
                        .verticalScroll(scroll)
                ) {
                    SelectionContainer {
                        Text(
                            text = jsonPreview,
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showJsonDialog = false
                }) { Text("Close") }
            },
            dismissButton = {
                TextButton(onClick = {
                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cm.setPrimaryClip(ClipData.newPlainText("MKumar JSON", jsonPreview))
                    scope.launch { snackbarHostState.showSnackbar("JSON copied") }
                }) {
                    Text("Copy")
                }
            }
        )
    }
}

@Composable
fun CustomerList(
    customers: List<CustomerFormState>,
    onClick: (CustomerFormState) -> Unit = {},
    onUpdateCustomer: (id: String, name: String, phone: String) -> Unit = { _, _, _ -> },
    onDelete: (CustomerFormState) -> Unit = {},
    onEdit: (CustomerFormState) -> Unit = {}                  // <— new
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(customers, key = { it.id }) { customer ->
            CustomerListCard2(
                customer = customer,
                onClick = onClick,
                onEdit = onEdit,                                // <—
                onDelete = onDelete
            )
        }
    }
}
