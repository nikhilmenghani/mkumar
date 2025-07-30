package com.mkumar.viewmodel

import androidx.lifecycle.ViewModel
import com.mkumar.data.CustomerFormState
import com.mkumar.data.ProductEntry
import com.mkumar.data.ProductFormData
import com.mkumar.data.ProductType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class CustomerViewModel : ViewModel() {

    private val _formState = MutableStateFlow(CustomerFormState())
    val formState: StateFlow<CustomerFormState> = _formState

    val editingBuffer = mutableMapOf<String, ProductFormData?>()

    fun updateCustomerName(name: String) {
        _formState.update { it.copy(name = name) }
    }

    fun updateCustomerPhone(phone: String) {
        _formState.update { it.copy(phone = phone) }
    }

    fun selectProduct(productId: String) {
        _formState.update { it.copy(selectedProductId = productId) }
    }

    fun addProduct(type: ProductType) {
        val newEntry = ProductEntry(
            type = type,
            productOwnerName = _formState.value.name // default owner to customer name
        )
        _formState.update {
            it.copy(
                products = it.products + newEntry,
                selectedProductId = newEntry.id
            )
        }
    }

    fun addNewProduct() {
        val dummyType = ProductType.allTypes.firstOrNull() ?: return
        val newEntry = ProductEntry(
            id = UUID.randomUUID().toString(),
            type = dummyType,
            productOwnerName = _formState.value.name
        )
        _formState.update {
            it.copy(
                products = it.products + newEntry,
                selectedProductId = newEntry.id
            )
        }
    }

    fun removeProduct(productId: String) {
        _formState.update {
            val updatedList = it.products.filterNot { product -> product.id == productId }
            val newSelected = if (it.selectedProductId == productId) null else it.selectedProductId
            it.copy(products = updatedList, selectedProductId = newSelected)
        }
        editingBuffer.remove(productId)
    }

    fun updateProductOwnerName(productId: String, newName: String) {
        _formState.update {
            val updated = it.products.map { product ->
                if (product.id == productId) product.copy(productOwnerName = newName) else product
            }
            it.copy(products = updated)
        }
    }

    fun saveProductFormData(productId: String, formData: ProductFormData) {
        _formState.update {
            val updated = it.products.map { product ->
                if (product.id == productId) product.copy(formData = formData, isSaved = true)
                else product
            }
            it.copy(products = updated)
        }
        editingBuffer.remove(productId)
    }

    fun hasUnsavedChanges(product: ProductEntry, editingBuffer: ProductFormData?): Boolean {
        return product.isSaved && product.formData != editingBuffer
    }

    fun getEditingProductData(product: ProductEntry): ProductFormData? {
        return editingBuffer[product.id] ?: product.formData
    }

    fun updateEditingBuffer(productId: String, formData: ProductFormData) {
        editingBuffer[productId] = formData
    }

    fun clearForm() {
        _formState.value = CustomerFormState()
    }

    fun serializeToJson(): String {
        // Placeholder - we'll later plug in kotlinx.serialization or Gson
        return "TODO: JSON representation"
    }
}
