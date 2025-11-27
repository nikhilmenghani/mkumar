package com.mkumar.ui.screens

import android.content.ClipData
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Badge
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
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
import com.mkumar.model.CustomerDetailsEffect
import com.mkumar.model.UiCustomerMini
import com.mkumar.network.VersionFetcher.fetchLatestVersion
import com.mkumar.ui.components.cards.CustomerListCard2
import com.mkumar.ui.components.dialogs.ConfirmActionDialog
import com.mkumar.ui.components.fabs.StandardFab
import com.mkumar.ui.components.sort.SortBar
import com.mkumar.ui.navigation.Material3BottomNavigationBar
import com.mkumar.ui.navigation.Routes
import com.mkumar.ui.navigation.Screen
import com.mkumar.ui.navigation.Screens
import com.mkumar.ui.screens.customer.humanReadableInvoiceLocation
import com.mkumar.viewmodel.CustomerViewModel
import com.mkumar.worker.DownloadWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
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
    val haptic = LocalHapticFeedback.current

    val snackbarHostState = remember { SnackbarHostState() }
    val (pendingDeleteOrderId, setPendingDeleteOrderId) = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            latestVersion = fetchLatestVersion()
            isLatestVersion = (currentVersion == latestVersion) || (latestVersion == "Unknown")
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
                                val hint = humanReadableInvoiceLocation(effect.orderId, effect.invoiceNumber)
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
                }
            }
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
                    if (!isLatestVersion) {
                        Box {
                            IconButton(
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
                                },
                                enabled = !isDownloading
                            ) {
                                if (isDownloading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.SystemUpdate,
                                        contentDescription = "Update",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Badge(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 4.dp, y = (-4).dp)   // fine-tuned overlap, not shifting the icon
                            ) {
                                Text(
                                    text = latestVersion,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1
                                )
                            }
                        }
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
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.onGloballyPositioned { coords ->
                    fabBlockHeight = with(density) { coords.size.height.toDp() }
                }) {
                StandardFab(
                    text = "",
                    icon = { Icon(Icons.Default.PersonSearch, contentDescription = "Search", modifier = Modifier.size(24.dp)) },
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        navController.navigate(Screen.Search.route)
                    },
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 12.dp)
        ) {

            DashboardSection(
                title = "Recent Orders",
                sortField = vm.orderSortBy.collectAsStateWithLifecycle().value,
                sortOrderAsc = vm.orderSortAsc.collectAsStateWithLifecycle().value,
                onSortFieldChange = { vm.setOrderSortBy(it) },
                onSortOrderChange = { vm.setOrderSortAsc(it) }
            ) {
                RecentOrdersList(
                    orders = vm.recentOrders.collectAsStateWithLifecycle().value,
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
            }
        }
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
            },
            onDismiss = { setPendingDeleteOrderId(null) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardSection(
    title: String,
    modifier: Modifier = Modifier,
    sortField: String,
    sortOrderAsc: Boolean,
    onSortFieldChange: (String) -> Unit,
    onSortOrderChange: (Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    SortBar(
        title = title,
        modifier = modifier,
        sortField = sortField,
        sortOrderAsc = sortOrderAsc,
        onSortFieldChange = onSortFieldChange,
        onSortOrderChange = onSortOrderChange
    )
    content()
}

@Composable
fun CustomerList(
    customers: List<UiCustomerMini>,
    onClick: (UiCustomerMini) -> Unit = {},
    onDelete: (UiCustomerMini) -> Unit = {},
    onEdit: (UiCustomerMini) -> Unit = {},
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
                onClick = { onClick(customer) },
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
@PreviewFontScale()
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        val navController = rememberNavController()

        Scaffold(
            topBar = {
                TopAppBar(title = { Text("M Kumar") })
            },
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
                        UiCustomerMini(id = "1", name = "John Doe", phone = "9876543210"),
                        UiCustomerMini(id = "2", name = "Jane Smith", phone = "8765432109"),
                        UiCustomerMini(id = "3", name = "Bob Wilson", phone = "7654321098")
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
