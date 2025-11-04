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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mkumar.ui.components.cards.OrderAccordionItem
import com.mkumar.ui.components.cards.OrderHeaderCard
import com.mkumar.ui.components.selectors.ProductSelector
import com.mkumar.viewmodel.CustomerDetailsIntent
import com.mkumar.viewmodel.CustomerDetailsUiState
import com.mkumar.viewmodel.CustomerDetailsViewModel
import com.mkumar.viewmodel.NewOrderIntent
import com.mkumar.viewmodel.ProductType
import java.time.LocalDate

@Composable
fun OrderSheet(
    state: CustomerDetailsUiState,
    viewModel: CustomerDetailsViewModel,
    modifier: Modifier = Modifier
) {
    val today = remember { LocalDate.now().toString() }
    val safeProducts = state.draft.items.orEmpty()
    var selectedType by remember { mutableStateOf(ProductType.Lens) }

    Surface(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()      // sized by parent; not unbounded
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            OrderHeaderCard(
                customerName = "Test Customer",
                date = today,
                mobile = "1234567890",
            )

            if (safeProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    Text(
                        text = "No products added yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                    )
                }
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                safeProducts.forEach { product ->
                    OrderAccordionItem(
                        selectedProduct = product,
                        selectedType = product.productType,
                        onFormSave = { productId, updatedProduct ->
                            viewModel.onNewOrderIntent(NewOrderIntent.FormUpdate(productId, updatedProduct))
                        },
                        onDelete = { productId ->
                            viewModel.onNewOrderIntent(NewOrderIntent.FormDelete(productId))
                        }
                    )
                }
                ProductSelector(
                    selectedType = selectedType,
                    onTypeSelected = { selectedType = it },
                    onAddClick = { type -> viewModel.onIntent(CustomerDetailsIntent.AddItem(type)) }
                )
            }
        }
    }
}