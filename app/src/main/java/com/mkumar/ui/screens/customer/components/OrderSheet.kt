package com.mkumar.ui.screens.customer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
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
import com.mkumar.ui.components.cards.OrderAccordionItem
import com.mkumar.ui.components.cards.OrderHeaderCardPro
import com.mkumar.ui.components.dialogs.DeleteProductConfirmDialog
import com.mkumar.ui.theme.AppColors
import com.mkumar.viewmodel.CustomerDetailsIntent
import com.mkumar.viewmodel.CustomerDetailsUiState
import com.mkumar.viewmodel.CustomerDetailsViewModel
import com.mkumar.viewmodel.NewOrderIntent
import java.time.LocalDate

@Composable
fun OrderSheet(
    state: CustomerDetailsUiState,
    viewModel: CustomerDetailsViewModel,
    modifier: Modifier = Modifier
) {
    val today = remember { LocalDate.now().toString() }
    val safeProducts = state.draft.items.orEmpty()
    val justAddedId = state.draft.justAddedItemId

    // NEW: hold which product is pending deletion
    var pendingDeleteId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(justAddedId) {
        if (justAddedId != null) {
            viewModel.onNewOrderIntent(NewOrderIntent.ConsumeJustAdded)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OrderHeaderCardPro(
            customerName = state.customer?.name ?: "Test Customer",
            mobile = state.customer?.phone ?: "1234567890",
            displayedDate = today,
            isDateReadOnly = false,
            onPickDateTime = { picked ->
                viewModel.onIntent(CustomerDetailsIntent.UpdateOccurredAt(picked))
            }
        )

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
                        text = "No products added yet. \nClick on '+' to Add a new Product.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .padding(20.dp)
                            .fillMaxWidth()
                    )
                }
            }
        } else {

            val groupRadius = 20.dp

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(groupRadius),
                colors = AppColors.elevatedCardColors(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.fillMaxWidth()) {
                    safeProducts.forEachIndexed { index, product ->
                        val isFirst = index == 0
                        val isLast = index == safeProducts.lastIndex

                        val radius = groupRadius
                        val rowShape = when {
                            isFirst && isLast -> RoundedCornerShape(radius)
                            isFirst -> RoundedCornerShape(
                                topStart = radius, topEnd = radius,
                                bottomStart = 0.dp, bottomEnd = 0.dp
                            )
                            isLast -> RoundedCornerShape(
                                topStart = 0.dp, topEnd = 0.dp,
                                bottomStart = radius, bottomEnd = radius
                            )
                            else -> RoundedCornerShape(0.dp)
                        }

                        OrderAccordionItem(
                            productOwner = state.customer?.name ?: "",
                            selectedProduct = product,
                            selectedType = product.productType,
                            onFormSave = { productId, updated ->
                                viewModel.onNewOrderIntent(
                                    NewOrderIntent.FormUpdate(productId, updated)
                                )
                            },
                            onDelete = { productId ->
                                // <- instead of deleting immediately, ask for confirmation
                                pendingDeleteId = productId
                            },
                            initiallyExpanded = (product.id == justAddedId),
                            grouped = true,
                            rowShape = rowShape
                        )

                        if (!isLast) {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .requiredHeight(1.dp)
                                    .background(MaterialTheme.colorScheme.outlineVariant)
                            )
                        }
                    }
                }
            }
        }
    }

    if (pendingDeleteId != null) {
        DeleteProductConfirmDialog(
            onConfirm = {
                pendingDeleteId?.let { id ->
                    viewModel.onNewOrderIntent(NewOrderIntent.FormDelete(id))
                }
                pendingDeleteId = null
            },
            onDismiss = {
                pendingDeleteId = null
            }
        )
    }
}

