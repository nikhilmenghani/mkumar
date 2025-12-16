package com.mkumar.ui.screens.customer.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mkumar.model.NewOrderIntent
import com.mkumar.model.OrderEditorIntent
import com.mkumar.model.OrderEditorUi
import com.mkumar.ui.components.cards.OrderHeaderCardPro
import com.mkumar.ui.components.dialogs.ConfirmActionDialog
import com.mkumar.viewmodel.OrderEditorViewModel

@Composable
fun OrderSheet(
    state: OrderEditorUi,
    viewModel: OrderEditorViewModel,
    modifier: Modifier = Modifier
) {
    val safeProducts = state.draft.items
    val justAddedId = state.draft.justAddedItemId

    var pendingDeleteId by remember { mutableStateOf<String?>(null) }

    // Consume just-added flag once
    LaunchedEffect(justAddedId) {
        if (justAddedId != null) {
            viewModel.onNewOrderIntent(NewOrderIntent.ConsumeJustAdded)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // -----------------------------------------------------------
        // HEADER CARD (unchanged)
        // -----------------------------------------------------------
        OrderHeaderCardPro(
            customerName = state.customer?.name ?: "",
            mobile = state.customer?.phone ?: "",
            invoiceNumber = state.draft.invoiceNumber.toString(),
            receivedAt = state.draft.receivedAt,   // <-- raw UTC millis
            isDateReadOnly = false,
            invoicePrefix = viewModel.getInvoicePrefix(),
            onPickDateTime = { pickedUtc ->
                viewModel.onIntent(OrderEditorIntent.UpdateOccurredAt(pickedUtc))
            }
        )

        // -----------------------------------------------------------
        // NO PRODUCTS YET
        // -----------------------------------------------------------
        if (safeProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    tonalElevation = 2.dp,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "No products added yet.\nClick on '+' to add a new product.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth()
                    )
                }
            }
        } else {

            // -----------------------------------------------------------
            // PRODUCTS SECTION CARD (DoorDash inspired)
            // -----------------------------------------------------------
            ProductsSectionCard(
                totalAmount = state.draft.totalAmount,
                adjustedAmount = state.draft.adjustedAmount,
                remainingBalance = state.draft.remainingBalance,
                paidTotal = state.draft.paidTotal,
                products = safeProducts,
                productOwner = state.customer?.name ?: "",
                onFormSave = { productId, updated ->
                    viewModel.onNewOrderIntent(
                        NewOrderIntent.FormUpdate(productId, updated)
                    )
                },
                onDelete = { productId ->
                    // Ask for confirmation — do not delete immediately
                    pendingDeleteId = productId
                },
                getTypeForProduct = { it.productType },  // your existing type source
                initiallyExpandedId = justAddedId,
                onAddPayment = { amountPaid, paymentAt ->
                    viewModel.onIntent(OrderEditorIntent.AddPayment(state.draft.orderId, amountPaid, paymentAt))
                },
                onDeletePayment = { paymentId ->
                    viewModel.onIntent(OrderEditorIntent.DeletePayment(paymentId))
                },
                payments = state.draft.payments,
                onAdjustedTotalChange = {
                    viewModel.onIntent(OrderEditorIntent.UpdateAdjustedAmount(it))
                },
            )
        }
    }

    // -----------------------------------------------------------
    // DELETE CONFIRMATION DIALOG
    // -----------------------------------------------------------
    if (pendingDeleteId != null) {
        ConfirmActionDialog(
            title = "Remove product?",
            message = "This product will be removed from the order. You can add it again later if needed.",
            confirmLabel = "Delete",
            dismissLabel = "Cancel",
            icon = Icons.Outlined.DeleteForever,
            highlightConfirmAsDestructive = true,
            onConfirm = {
                pendingDeleteId?.let { id ->
                    viewModel.onNewOrderIntent(NewOrderIntent.FormDelete(id))
                }
                pendingDeleteId = null
            },
            onDismiss = { pendingDeleteId = null }
        )
    }
}

