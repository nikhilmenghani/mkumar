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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mkumar.data.ProductFormData
import com.mkumar.data.validation.ValidationResult
import com.mkumar.ui.components.buttons.ProductActionButtons
import com.mkumar.viewmodel.ProductType
import com.mkumar.viewmodel.UiOrderItem

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ProductFormItem(
    selectedProduct: UiOrderItem,
    draft: ProductFormData,
    onDraftChange: (ProductFormData) -> Unit,
    onSave: (String, ProductFormData) -> Unit,
    onDelete: (String) -> Unit,
) {

    AnimatedContent(
        targetState = selectedProduct,
        transitionSpec = { fadeIn().togetherWith(fadeOut()) },
        label = "ProductFormContent"
    ) { product ->
        key(product.id) {

            var validationError by remember(product.id) {
                mutableStateOf<String?>(null)
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = draft.productOwner,
                    onValueChange = {
                        onDraftChange(
                            defaultFormFor(product.productType, it)
                        )
                    },
                    label = { Text("Product Owner") },
                    modifier = Modifier
                        .fillMaxWidth()
                )

                validationError?.let {
                    Text(it, color = Color.Red)
                }

                RenderProductForm(
                    productType = product.productType,
                    productFormData = draft,
                    updateProductFormData = { onDraftChange(it) },
                )

                ProductActionButtons(
                    productId = product.id,
                    draft = draft,
                    onDelete = onDelete,
                    onSave = { id, draftData ->
                        onSave(id, draftData)
                    }
                )
            }
        }
    }
}

@Composable
private fun RenderProductForm(
    productType: ProductType,
    productFormData: ProductFormData?,
    updateProductFormData: (ProductFormData) -> Unit
) {
    when (productType) {
        ProductType.Frame -> FrameForm(
            initialData = productFormData as? ProductFormData.FrameData,
            onChange = updateProductFormData,
        )

        ProductType.Lens -> LensForm(
            initialData = productFormData as? ProductFormData.LensData,
            onChange = updateProductFormData,
        )

        ProductType.ContactLens -> ContactLensForm(
            initialData = productFormData as? ProductFormData.ContactLensData,
            onChange = updateProductFormData,
        )
    }
}

fun defaultFormFor(type: ProductType, productOwner: String): ProductFormData = when (type) {
    ProductType.Frame -> ProductFormData.FrameData().copy(productOwner = productOwner)
    ProductType.Lens -> ProductFormData.LensData().copy(productOwner = productOwner)
    ProductType.ContactLens -> ProductFormData.ContactLensData().copy(productOwner = productOwner)
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
