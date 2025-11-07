package com.mkumar.ui.screens.customer.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mkumar.ui.components.cards.OrderAccordionItem
import com.mkumar.ui.components.cards.OrderHeaderCardPro
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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),         // keep alignment tidy
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header stays aligned with same padding as other rows
        OrderHeaderCardPro(
            customerName = state.customer?.name ?: "Test Customer",
            mobile = state.customer?.phone ?: "1234567890",
            displayedDate = today,
            isDateReadOnly = false,               // or true if readonly
            onPickDateTime = { picked ->
                // if you already handle date via ViewModel, call that here instead
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
            // Plain Column + forEach is fine inside a non-scrollable parent
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                safeProducts.forEach { product ->
                    OrderAccordionItem(
                        selectedProduct = product,
                        selectedType = product.productType,
                        onFormSave = { productId, updated ->
                            viewModel.onNewOrderIntent(NewOrderIntent.FormUpdate(productId, updated))
                        },
                        onDelete = { productId ->
                            viewModel.onNewOrderIntent(NewOrderIntent.FormDelete(productId))
                        }
                    )
                }
            }
        }
    }
}
