package com.mkumar.ui.components.forms

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mkumar.data.ProductEntry
import com.mkumar.data.ProductFormData
import com.mkumar.data.ProductType
import com.mkumar.data.validation.ProductFormValidators
import com.mkumar.data.validation.ValidationResult
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ProductFormItem(
    selectedProduct: ProductEntry,
    productFormData: ProductFormData?,
    updateProductFormData: (String, ProductFormData) -> Unit,
//    onOwnerChange: (String, String) -> Unit,
    hasUnsavedChanges: (ProductEntry, ProductFormData?) -> Boolean,
//    onFormSave: (String, ProductFormData) -> Unit
) {
//    if (selectedProduct == null) return
//    val productFormData by rememberUpdatedState(getProductFormData(selectedProduct))
//    val isDirty by remember(selectedProduct.id, productFormData) {
//        derivedStateOf {
//            !selectedProduct.isSaved || hasUnsavedChanges(selectedProduct, productFormData)
//        }
//    }

    AnimatedContent(
        targetState = selectedProduct,
        transitionSpec = { fadeIn().togetherWith(fadeOut()) },
        label = "ProductFormContent"
    ) { product ->
        key(product.id) {
            var ownerName by rememberSaveable(product.id) {
                mutableStateOf(product.productOwnerName)
            }
            var validationError by remember(product.id) {
                mutableStateOf<String?>(null)
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = ownerName,
                    onValueChange = { ownerName = it },
                    label = { Text("Product Owner") },
                    modifier = Modifier
                        .fillMaxWidth()
                )

                validationError?.let {
                    Text(it, color = Color.Red)
                }

                RenderProductForm(
                    product = product,
                    productFormData = productFormData,
                    updateProductFormData = {
                        updateProductFormData(product.id, it)
                    },
//                    showSaveButton = isDirty,
//                    onSave = { formData ->
//                        validateAndSave(
//                            formData = formData,
//                            validate = ProductFormValidators::validate,
//                            onSuccess = {
//                                validationError = null
//                                onOwnerChange(product.id, ownerName)
//                                onFormSave(product.id, formData)
//                            },
//                            onError = { errorMsg -> validationError = errorMsg }
//                        )
//                    }
                )
            }
        }
    }
}

@Composable
private fun RenderProductForm(
    product: ProductEntry,
    productFormData: ProductFormData?,
    updateProductFormData: (ProductFormData) -> Unit,
//    showSaveButton: Boolean,
//    onSave: (ProductFormData) -> Unit
) {
    when (product.productType) {
        is ProductType.Frame -> FrameForm(
            initialData = productFormData as? ProductFormData.FrameData,
            onChange = updateProductFormData,
//            showSave = showSaveButton,
//            onSave = onSave
        )

        is ProductType.Lens -> LensForm(
            initialData = productFormData as? ProductFormData.LensData,
            onChange = updateProductFormData,
//            showSave = showSaveButton,
//            onSave = onSave
        )

        is ProductType.ContactLens -> ContactLensForm(
            initialData = productFormData as? ProductFormData.ContactLensData,
            onChange = updateProductFormData,
//            showSave = showSaveButton,
//            onSave = onSave
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
