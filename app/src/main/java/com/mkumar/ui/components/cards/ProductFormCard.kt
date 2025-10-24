package com.mkumar.ui.components.cards

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mkumar.data.ProductEntry
import com.mkumar.data.ProductFormData
import com.mkumar.data.ProductType
import com.mkumar.ui.components.forms.ProductFormSwitcher
import com.mkumar.ui.components.selectors.ProductSelector
import kotlinx.coroutines.flow.StateFlow


@Composable
fun ProductFormCard(
    availableTypes: List<ProductType>,
    selectedType: ProductType?,
    onTypeSelected: (ProductType) -> Unit,
    onAddClick: (ProductType) -> Unit,
    selectedProduct: ProductEntry?,
    openForms: StateFlow<String>,
    getEditingBuffer: (ProductEntry) -> ProductFormData?,
    updateEditingBuffer: (String, ProductFormData) -> Unit,
    onOwnerChange: (String, String) -> Unit,
    hasUnsavedChanges: (ProductEntry, ProductFormData?) -> Boolean,
    onFormSave: (String, ProductFormData) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Add a new Product",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            ProductSelector(
                availableTypes = availableTypes,
                selectedType = selectedType,
                onTypeSelected = onTypeSelected,
                onAddClick = onAddClick
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Form switcher (only for this customer's open forms)
            ProductFormSwitcher(
                selectedProduct = selectedProduct,
                openForms = openForms,
                getEditingBuffer = getEditingBuffer,
                updateEditingBuffer = updateEditingBuffer,
                onOwnerChange = onOwnerChange,
                hasUnsavedChanges = hasUnsavedChanges,
                onFormSave = onFormSave
            )
        }
    }
}

