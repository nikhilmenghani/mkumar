package com.mkumar.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mkumar.data.ProductEntry
import com.mkumar.data.ProductFormData
import com.mkumar.data.ProductType
import com.mkumar.ui.components.selectors.ProductSelector
import com.mkumar.ui.components.cards.OrderAccordionItem
import com.mkumar.ui.components.cards.OrderHeaderCard
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderEntryAccordionScreen(
    customerName: String,
    phoneNumber: String,
    availableTypes: List<ProductType>,
    selectedType: ProductType?,
    onTypeSelected: (ProductType) -> Unit,
    onAddClick: (ProductType) -> Unit,
    products: List<ProductEntry>?,
    getProductFormData: (ProductEntry) -> ProductFormData?,
    updateProductFormData: (String, ProductFormData) -> Unit,
    onOwnerChange: (String, String) -> Unit,
    hasUnsavedChanges: (ProductEntry, ProductFormData?) -> Boolean,
    onFormSave: (String, ProductFormData) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = remember { LocalDate.now().toString() }
    val safeProducts = products.orEmpty()

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
    Surface(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()      // sized by parent; not unbounded
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            OrderHeaderCard(
                customerName = customerName,
                date = today,
                mobile = phoneNumber
            )

            safeProducts.forEach { product ->
                OrderAccordionItem(
                    selectedProduct = product,
                    selectedType = product.productType,
                    getProductFormData = getProductFormData,
                    updateProductFormData = updateProductFormData,
                    hasUnsavedChanges = hasUnsavedChanges,
                    onFormSave = onFormSave
                )
            }

            ProductSelector(
                availableTypes = availableTypes,
                selectedType = selectedType,
                onTypeSelected = onTypeSelected,
                onAddClick = onAddClick
            )

            OutlinedButton(
                onClick = { selectedType?.let { onAddClick(it) } },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = "Add Product")
                    Spacer(Modifier.width(8.dp))
                    Text("Add New Item")
                }
            }
        }
    }

}
