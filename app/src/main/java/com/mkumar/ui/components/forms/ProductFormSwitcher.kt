package com.mkumar.ui.components.forms

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.mkumar.data.ProductEntry
import com.mkumar.data.ProductFormData
import com.mkumar.data.ProductType
import com.mkumar.data.validation.ProductFormValidators
import com.mkumar.data.validation.ValidationResult

@Composable
fun ProductFormSwitcher(
    selectedProduct: ProductEntry?,
    getEditingBuffer: (ProductEntry) -> ProductFormData?, // from VM
    updateEditingBuffer: (String, ProductFormData) -> Unit,
    onOwnerChange: (String, String) -> Unit,
    onFormSave: (String, ProductFormData) -> Unit
) {
    if (selectedProduct == null) {
        Text("Select a product to fill in details.")
        return
    }

    var ownerName by remember { mutableStateOf("") }
    var validationError by remember(selectedProduct.id) { mutableStateOf<String?>(null) }

    // Prefill ownerName on product switch
    LaunchedEffect(selectedProduct.id) {
        ownerName = selectedProduct.productOwnerName
    }

    Column {
        OutlinedTextField(
            value = ownerName,
            onValueChange = {
                ownerName = it
                onOwnerChange(selectedProduct.id, it)
            },
            label = { Text("Product Owner") },
            modifier = Modifier.fillMaxWidth()
        )

        // Validation error
        validationError?.let {
            Text(text = it, color = Color.Red)
        }

        AnimatedContent(
            targetState = selectedProduct,
            label = "ProductFormTransition"
        ) { product ->
            key(product.id) {
                RenderProductForm(
                    product = selectedProduct,
                    editingFormData = getEditingBuffer(selectedProduct),
                    onFormChanged = { formData ->
                        updateEditingBuffer(selectedProduct.id, formData)
                    },
                    onValidatedSave = { formData ->
                        validateAndSave(
                            formData = formData,
                            validate = ProductFormValidators::validate,
                            onSuccess = {
                                validationError = null
                                onFormSave(product.id, formData)
                            },
                            onError = { validationError = it }
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun RenderProductForm(
    product: ProductEntry,
    editingFormData: ProductFormData?,
    onFormChanged: (ProductFormData) -> Unit,
    onValidatedSave: (ProductFormData) -> Unit
) {
    when (product.type) {
        is ProductType.Frame -> FrameForm(
            initialData = editingFormData as? ProductFormData.FrameData,
            onChange = { onFormChanged(it) },
            onSave = onValidatedSave
        )

        is ProductType.Lens -> LensForm(
            initialData = editingFormData as? ProductFormData.LensData,
            onChange = { onFormChanged(it) },
            onSave = onValidatedSave
        )

        is ProductType.ContactLens -> ContactLensForm(
            initialData = editingFormData as? ProductFormData.ContactLensData,
            onChange = { onFormChanged(it) },
            onSave = onValidatedSave
        )
    }
}

private fun <T : ProductFormData> validateAndSave(
    formData: T,
    validate: (T) -> ValidationResult,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val result = validate(formData)
    if (result.isValid) {
        onSuccess()
    } else {
        onError(result.errors.values.joinToString("\n"))
    }
}
