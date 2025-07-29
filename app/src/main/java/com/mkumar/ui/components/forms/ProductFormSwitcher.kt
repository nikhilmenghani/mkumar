package com.mkumar.ui.components.forms

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mkumar.data.ProductEntry
import com.mkumar.data.ProductFormData
import com.mkumar.data.ProductType
import com.mkumar.data.validation.ProductFormValidators
import com.mkumar.data.validation.ValidationResult

@OptIn(ExperimentalAnimationApi::class)
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

    var ownerName by remember(selectedProduct.id) { mutableStateOf(selectedProduct.productOwnerName) }
    var validationError by remember(selectedProduct.id) { mutableStateOf<String?>(null) }

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

        // Validation error
        validationError?.let {
            Text(text = it, color = Color.Red)
        }

        // Animated form content
        AnimatedContent(
            targetState = selectedProduct,
            transitionSpec = { fadeIn() with fadeOut() },
            label = "Product Form Transition"
        ) { product ->
            key(product.id) {
                RenderProductForm(
                    product = product,
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
private fun RenderProductForm(
    product: ProductEntry,
    onValidatedSave: (ProductFormData) -> Unit
) {
    when (product.type) {
        is ProductType.Frame -> FrameForm(
            initialData = product.formData as? ProductFormData.FrameData,
            onSave = onValidatedSave
        )

        is ProductType.Lens -> LensForm(
            initialData = product.formData as? ProductFormData.LensData,
            onSave = onValidatedSave
        )

        is ProductType.ContactLens -> ContactLensForm(
            initialData = product.formData as? ProductFormData.ContactLensData,
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
