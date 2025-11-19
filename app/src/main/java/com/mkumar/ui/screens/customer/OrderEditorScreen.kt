package com.mkumar.ui.screens.customer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.mkumar.model.OrderEditorEffect
import com.mkumar.model.OrderEditorIntent
import com.mkumar.model.ProductType
import com.mkumar.ui.components.accordions.OrderSummaryAccordion
import com.mkumar.ui.components.bottomsheets.ProductPickerSheet
import com.mkumar.ui.components.fabs.AddProductSpeedMenuButton
import com.mkumar.ui.screens.customer.components.OrderSheet
import com.mkumar.viewmodel.OrderEditorViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderEditorScreen(
    navController: NavHostController,
    viewModel: OrderEditorViewModel,
    customerId: String,
    editingOrderId: String
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showProductPicker by remember { mutableStateOf(false) }
    val imeBottom = WindowInsets.ime.getBottom(LocalDensity.current)
    val imeVisible = imeBottom > 0
    val barHeight = 64.dp

    // Kick off draft load/create once on enter
    LaunchedEffect(customerId, editingOrderId) {
        viewModel.load(customerId, editingOrderId)
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collectLatest { effect ->
            when (effect) {
                OrderEditorEffect.CloseEditor -> {
//                    navController.previousBackStackEntry
//                        ?.savedStateHandle?.set("orderSaved", ui.draft.orderId)
                    navController.popBackStack()
                }

                is OrderEditorEffect.ShowMessage ->
                    snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    ProductPickerSheet(
        isOpen = showProductPicker,
        onDismiss = { showProductPicker = false },
        allTypes = ProductType.entries.toList(),
        onAddClick = { type ->
            viewModel.onIntent(OrderEditorIntent.AddItem(type))
            showProductPicker = false
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (editingOrderId.isBlank()) "New Sale" else "Edit Order",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Optional: Add quick "Add item" icon or overflow
                }
            )
        },
        bottomBar = {
            if (!imeVisible) {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    tonalElevation = 0.dp
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .imePadding()              // avoid keyboard overlap
                            .navigationBarsPadding()   // avoid gesture area
                            .padding(start = 12.dp, end = 24.dp, top = 12.dp, bottom = 12.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AddProductSpeedMenuButton(
                            commonTypes = ProductType.entries.toList(),
                            lastUsed = null,
                            onAddClick = { type ->
                                viewModel.onIntent(OrderEditorIntent.AddItem(type))
                            },
                            onOpenPicker = { showProductPicker = true }
                        )
                        if (ui.draft.items.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(12.dp))
                            FloatingActionButton(
                                onClick = { viewModel.onIntent(OrderEditorIntent.SaveOrder) },
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(56.dp)
                            ) {
                                Icon(Icons.Default.DoneAll, contentDescription = "Save")
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        FloatingActionButton(
                            onClick = { navController.popBackStack() },
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Dismiss"
                            )
                        }
                    }
                }
            }

        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .imePadding()
        ) {
            if (ui.isLoading) LinearProgressIndicator(Modifier.fillMaxWidth())
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
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
                            onAdjustedAmountChange = {
                                viewModel.onIntent(OrderEditorIntent.UpdateAdjustedAmount(it))
                            },
                            advanceTotal = ui.draft.advanceTotal,
                            onAdvanceTotalChange = {
                                viewModel.onIntent(OrderEditorIntent.UpdateAdvanceTotal(it))
                            },
                            remainingBalance = ui.draft.remainingBalance,
                            initiallyExpanded = false
                        )
                    }
                }
            }
        }
    }
}
