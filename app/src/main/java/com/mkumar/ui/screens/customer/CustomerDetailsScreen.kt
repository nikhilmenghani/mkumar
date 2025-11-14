package com.mkumar.ui.screens.customer

import android.annotation.SuppressLint
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.mkumar.R
import com.mkumar.common.constant.CustomerDetailsConstants
import com.mkumar.ui.components.dialogs.ConfirmActionDialog
import com.mkumar.ui.navigation.Routes
import com.mkumar.ui.screens.customer.components.CustomerHeader
import com.mkumar.ui.screens.customer.components.OrderList
import com.mkumar.viewmodel.CustomerDetailsEffect
import com.mkumar.viewmodel.CustomerDetailsIntent
import com.mkumar.viewmodel.CustomerDetailsViewModel
import com.mkumar.viewmodel.CustomerHeaderUi
import com.mkumar.viewmodel.OrderRowAction
import com.mkumar.viewmodel.toOrderRowUi
import kotlinx.coroutines.flow.collectLatest

@SuppressLint("LocalContextResourcesRead")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailsScreen(
    navController: NavHostController,
    viewModel: CustomerDetailsViewModel,
    onBack: () -> Unit = { navController.popBackStack() },
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val drawable = androidx.core.content.ContextCompat.getDrawable(context, R.mipmap.ic_launcher)
    requireNotNull(drawable) { "Launcher icon drawable missing" }
    val logo = when (drawable) {
        is android.graphics.drawable.BitmapDrawable -> drawable.bitmap
        else -> {
            val w = drawable.intrinsicWidth.takeIf { it > 0 } ?: 256
            val h = drawable.intrinsicHeight.takeIf { it > 0 } ?: 256
            val bmp = createBitmap(w, h)
            val canvas = android.graphics.Canvas(bmp)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bmp
        }
    }
    // Snackbar + one-off effects
    val snackbarHostState = remember { SnackbarHostState() }
    val (pendingDeleteOrderId, setPendingDeleteOrderId) = remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
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
                        clipData = android.content.ClipData.newRawUri("invoice", effect.uri)
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
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ExtendedFloatingActionButton(
                    onClick = {
                        val cid = ui.customer?.id.orEmpty()
                        navController.navigate(Routes.orderEditor(customerId = cid))
                    },
                    icon = { Icon(Icons.Outlined.AddShoppingCart, contentDescription = null) },
                    text = { Text("New Sale") }
                )
            }

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
                    customer = ui.customer,
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
                        o.toOrderRowUi()
                    }
                }
                OrderList(
                    orders = rows,
                    onAction = { action ->
                        when (action) {
                            is OrderRowAction.Open -> {
                                val cid = ui.customer?.id.orEmpty()
                                navController.navigate(Routes.orderEditor(customerId = cid, orderId = action.orderId))
                            }

                            is OrderRowAction.Delete -> {
                                setPendingDeleteOrderId(action.orderId)
                            }
                            is OrderRowAction.Share -> viewModel.onIntent(CustomerDetailsIntent.ShareOrder(action.orderId, action.invoiceNumber, logo))
                            is OrderRowAction.ViewInvoice -> viewModel.onIntent(CustomerDetailsIntent.ViewInvoice(action.orderId, action.invoiceNumber, logo))
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                if (pendingDeleteOrderId != null) {
                    ConfirmActionDialog(
                        title = "Delete Order",
                        message = "This action cannot be undone. Delete this order?",
                        confirmLabel = "Delete",
                        dismissLabel = "Cancel",
                        highlightConfirmAsDestructive = true,
                        onConfirm = {
                            viewModel.onIntent(CustomerDetailsIntent.DeleteOrder(pendingDeleteOrderId))
                            setPendingDeleteOrderId(null)
                        },
                        onDismiss = { setPendingDeleteOrderId(null) }
                    )
                }
            }
        }
    }
}

fun humanReadableInvoiceLocation(orderId: String, invoiceNumber: String): String {
    val fileName = CustomerDetailsConstants.getInvoiceFileName(orderId = orderId, invoiceNumber = invoiceNumber) + ".pdf"
    return "Files > Downloads > Documents > MKumar > Invoices > $fileName"
}
