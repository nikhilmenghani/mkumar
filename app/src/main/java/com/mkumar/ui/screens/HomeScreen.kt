package com.mkumar.ui.screens

import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
import com.mkumar.data.ProductType
import com.mkumar.network.VersionFetcher.fetchLatestVersion
import com.mkumar.ui.components.bottomsheets.BaseBottomSheet
import com.mkumar.ui.components.chips.ProductChipRow
import com.mkumar.ui.components.fabs.StandardFab
import com.mkumar.ui.components.forms.ProductFormSwitcher
import com.mkumar.ui.components.inputs.CustomerInfoSection
import com.mkumar.ui.components.selectors.ProductSelector
import com.mkumar.ui.navigation.Screens
import com.mkumar.viewmodel.CustomerViewModel
import com.mkumar.worker.DownloadWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

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
    var showCustomerDialog by remember { mutableStateOf(false) }

    // ViewModel state
    val customers by vm.customers.collectAsStateWithLifecycle()
    val currentCustomerId by vm.currentCustomerId.collectAsStateWithLifecycle()
    val currentCustomer = remember(customers, currentCustomerId) {
        customers.firstOrNull { it.id == currentCustomerId }
    }
    val openFormsForCurrentFlow = remember(currentCustomerId) { MutableStateFlow(emptySet<String>()) }
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
                    text = if (customers.size >= 3) "Max 3 customers" else "Add a new Customer",
                    icon = { Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(24.dp)) },
                    onClick = { if (customers.size < 3) showAddCustomerSheet = true },
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
                    showCustomerDialog = true
                }
            )
        }
    }

    // --- Add Customer Sheet (local inputs; VM is multi-customer now) ---
    if (showAddCustomerSheet) {
        var name by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }

        BaseBottomSheet(
            title = "Add Customer",
            sheetContent = {
                CustomerInfoSection(
                    name = name,
                    phone = phone,
                    onNameChange = { name = it },
                    onPhoneChange = { phone = it }
                )
            },
            onDismiss = { showAddCustomerSheet = false },
            showDismiss = true,
            showDone = true,
            onDoneClick = {
                if (name.isNotBlank()) {
                    vm.addCustomer(name.trim(), phone.trim())
                    showAddCustomerSheet = false
                }
            }
        )
    }

    // --- Customer Popup: add products for the selected customer ---
    if (showCustomerDialog && currentCustomer != null) {
        var showSnackbar by remember { mutableStateOf(false) }
        val snackbarHostState = remember { SnackbarHostState() }
        LaunchedEffect(showSnackbar) {
            if (showSnackbar) {
                snackbarHostState.showSnackbar("Product saved!")
                showSnackbar = false
            }
        }

        val selectedProductType = remember { mutableStateOf<ProductType?>(null) }
        val cId = currentCustomer.id

        BaseBottomSheet(
            title = currentCustomer.name.ifBlank { "Customer" },
            sheetContent = {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .verticalScroll(scrollState)
                        .padding(bottom = 72.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Product Type selection + Add
                    ProductSelector(
                        availableTypes = ProductType.allTypes,
                        selectedType = selectedProductType.value,
                        onTypeSelected = { selectedProductType.value = it },
                        onAddClick = { type -> vm.addProduct(cId, type) }   // <-- scoped by customerId
                    )

                    // Chips for this customer's products
                    ProductChipRow(
                        products = currentCustomer.products,
                        selectedId = currentCustomer.selectedProductId,
                        onChipClick = { productId -> vm.openForm(cId, productId) },
                        onChipDelete = { productId -> vm.removeProduct(cId, productId) },
                        getCurrentBuffer = { product -> vm.getEditingProductData(cId, product) },
                        hasUnsavedChanges = { product, buf -> vm.hasUnsavedChanges(cId, product, buf) }
                    )

                    // Form switcher (only for this customer's open forms)
                    ProductFormSwitcher(
                        selectedProduct = currentCustomer.products.find { it.id == currentCustomer.selectedProductId },
                        openForms = openFormsForCurrentFlow, // Set<String>
                        getEditingBuffer = { product -> vm.getEditingProductData(cId, product) },
                        updateEditingBuffer = { productId, data -> vm.updateEditingBuffer(cId, productId, data) },
                        onOwnerChange = { productId, newName -> vm.updateProductOwnerName(cId, productId, newName) },
                        hasUnsavedChanges = { product, buf -> vm.hasUnsavedChanges(cId, product, buf) },
                        onFormSave = { productId, data ->
                            vm.saveProductFormData(cId, productId, data)
                            showSnackbar = true
                        }
                    )
                }
            },
            onDismiss = { showCustomerDialog = false },
            showDismiss = true,
        )
    }
}

@Composable
fun CustomerList(
    customers: List<CustomerFormState>,
    onClick: (CustomerFormState) -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(customers, key = { it.id }) { customer ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                onClick = { onClick(customer) }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Customer ID: ${customer.id}")
                    Text(text = "Name: ${customer.name}")
                    Text(text = "Phone: ${customer.phone}")
                }
            }
        }
    }
}
