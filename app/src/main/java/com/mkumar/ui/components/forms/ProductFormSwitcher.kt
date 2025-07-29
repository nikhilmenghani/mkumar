package com.mkumar.ui.components.forms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mkumar.data.ProductEntry
import com.mkumar.data.ProductFormData
import com.mkumar.data.ProductType

@Composable
fun ProductFormSwitcher(
    selectedProduct: ProductEntry?,
    onOwnerChange: (String, String) -> Unit,
    onFormSave: (String, ProductFormData) -> Unit
) {
    if (selectedProduct == null) {
        Text("Select a product to fill in details.")
        return
    }

    var ownerName by remember { mutableStateOf(selectedProduct.productOwnerName) }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = ownerName,
            onValueChange = {
                ownerName = it
                onOwnerChange(selectedProduct.id, it)
            },
            label = { Text("Product Owner") },
            modifier = Modifier.fillMaxWidth()
        )

        when (selectedProduct.type) {
            is ProductType.Frame -> FrameForm(
                initialData = selectedProduct.formData as? ProductFormData.FrameData,
                onSave = { formData ->
                    onFormSave(selectedProduct.id, formData)
                }
            )
            is ProductType.Lens -> LensForm(
                initialData = selectedProduct.formData as? ProductFormData.LensData,
                onSave = { formData ->
                    onFormSave(selectedProduct.id, formData)
                }
            )
            is ProductType.ContactLens -> ContactLensForm(
                initialData = selectedProduct.formData as? ProductFormData.ContactLensData,
                onSave = { formData ->
                    onFormSave(selectedProduct.id, formData)
                }
            )
        }
    }
}
