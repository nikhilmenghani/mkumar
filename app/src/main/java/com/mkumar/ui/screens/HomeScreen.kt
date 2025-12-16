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
import androidx.compose.material3.SnackbarHost
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
import androidx.compose.ui.unit.Dp
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
import com.mkumar.model.CustomerDetailsEffect
import com.mkumar.model.UiCustomerMini
import com.mkumar.ui.components.cards.CustomerListCard2
import com.mkumar.ui.components.dialogs.ConfirmActionDialog
import com.mkumar.ui.components.fabs.StandardFab
import com.mkumar.ui.components.sort.SortBar
import com.mkumar.ui.navigation.Routes
import com.mkumar.ui.navigation.Screen
import com.mkumar.ui.navigation.Screens
import com.mkumar.viewmodel.CustomerViewModel
import com.mkumar.worker.DownloadWorker
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    vm: CustomerViewModel
) {
    val context = LocalActivity.current as MainActivity
    val workManager = WorkManager.getInstance(context)

    val homeUi by vm.homeUi.collectAsStateWithLifecycle()
    val recentOrders by vm.recentOrders.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current

    var fabBlockHeight by remember { mutableStateOf(0.dp) }
    var pendingDeleteOrderId by remember { mutableStateOf<String?>(null) }

    /* ------------------------------------------------------------------ */
    /* BOOTSTRAP + EFFECTS                                                 */
    /* ------------------------------------------------------------------ */

    LaunchedEffect(Unit) {
        // 🔑 This restores "update icon on first load"
        vm.bootstrap(getCurrentVersion(context))

        vm.effects.collectLatest { effect ->
            when (effect) {
                is CustomerDetailsEffect.ShowMessage ->
                    snackbarHostState.showSnackbar(effect.message)

                is CustomerDetailsEffect.ViewInvoice -> {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(effect.uri, "application/pdf")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    runCatching {
                        context.startActivity(Intent.createChooser(intent, "Open invoice"))
                    }.onFailure {
                        snackbarHostState.showSnackbar(
                            "Invoice saved locally. No PDF viewer found."
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
                    runCatching {
                        context.startActivity(Intent.createChooser(send, "Share invoice"))
                    }.onFailure {
                        snackbarHostState.showSnackbar("No app available to share PDF")
                    }
                }

                else -> Unit
            }
        }
    }

    /* ------------------------------------------------------------------ */
    /* UI                                                                 */
    /* ------------------------------------------------------------------ */

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("M Kumar")
                        if (homeUi.currentVersion.isNotBlank()) {
                            Text(
                                text = "v${homeUi.currentVersion}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {

                    // Manual refresh (still supported)
                    IconButton(onClick = vm::checkVersion) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }

                    // 🔄 Update available
                    if (!homeUi.isLatest) {
                        Box {
                            IconButton(
                                enabled = !homeUi.isDownloading,
                                onClick = {
                                    val downloadUrl =
                                        getAppDownloadUrl(homeUi.latestVersion)
                                    val destPath =
                                        "${getExternalStorageDir()}/Download/MKumar.apk"

                                    val request =
                                        OneTimeWorkRequestBuilder<DownloadWorker>()
                                            .setInputData(
                                                workDataOf(
                                                    DownloadWorker.DOWNLOAD_URL_KEY to downloadUrl,
                                                    DownloadWorker.DEST_FILE_PATH_KEY to destPath,
                                                    DownloadWorker.DOWNLOAD_TYPE_KEY to
                                                            DownloadWorker.DOWNLOAD_TYPE_APK
                                                )
                                            )
                                            .setConstraints(
                                                Constraints.Builder()
                                                    .setRequiredNetworkType(NetworkType.CONNECTED)
                                                    .build()
                                            )
                                            .build()

                                    vm.setDownloading(true)
                                    workManager.enqueue(request)

                                    workManager.getWorkInfoByIdLiveData(request.id)
                                        .observeForever { info ->
                                            when (info?.state) {
                                                WorkInfo.State.SUCCEEDED -> {
                                                    vm.setDownloading(false)
                                                    if (context.packageManager
                                                            .canRequestPackageInstalls()
                                                    ) {
                                                        installApk(context, destPath)
                                                    }
                                                }

                                                WorkInfo.State.FAILED -> {
                                                    vm.setDownloading(false)
                                                    Toast.makeText(
                                                        context,
                                                        "Download failed",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }

                                                else -> Unit
                                            }
                                        }
                                }
                            ) {
                                if (homeUi.isDownloading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(Icons.Default.SystemUpdate, "Update")
                                }
                            }

                            Badge(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 4.dp, y = (-4).dp)
                            ) {
                                Text(homeUi.latestVersion)
                            }
                        }
                    }

                    IconButton(
                        onClick = {
                            navController.navigateWithState(Screens.Settings.name)
                        }
                    ) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.onGloballyPositioned {
                    fabBlockHeight = with(density) { it.size.height.toDp() }
                }
            ) {
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
                        navController.navigate(Screen.Search.route)
                    }
                )
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 12.dp)
        ) {

            DashboardSection(
                title = "Recent Orders",
                sortField = vm.orderSortBy.collectAsStateWithLifecycle().value,
                sortOrderAsc = vm.orderSortAsc.collectAsStateWithLifecycle().value,
                onSortFieldChange = vm::setOrderSortBy,
                onSortOrderChange = vm::setOrderSortAsc
            ) {
                RecentOrdersList(
                    orders = recentOrders,
                    invoicePrefix = vm.getInvoicePrefix(),
                    onOrderClick = { orderId, customerId ->
                        navController.navigate(
                            Routes.orderEditor(customerId, orderId)
                        )
                    },
                    onInvoiceClick = { orderId, invoice ->
                        vm.viewInvoice(orderId, invoice.toString())
                    },
                    onShareClick = { orderId, invoice ->
                        vm.shareInvoice(orderId, invoice.toString())
                    },
                    onDeleteClick = { pendingDeleteOrderId = it },
                    onOpenCustomer = {
                        navController.navigate(Routes.customerDetail(it))
                    }
                )
            }
        }
    }

    pendingDeleteOrderId?.let { orderId ->
        ConfirmActionDialog(
            title = "Delete Order",
            message = "This action cannot be undone.",
            confirmLabel = "Delete",
            dismissLabel = "Cancel",
            highlightConfirmAsDestructive = true,
            onConfirm = {
                vm.deleteOrder(orderId)
                pendingDeleteOrderId = null
            },
            onDismiss = { pendingDeleteOrderId = null }
        )
    }
}

/* -------------------------------------------------------------------------- */

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