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
    private val _openForms = MutableStateFlow<Set<String>>(emptySet())
    val openForms: StateFlow<Set<String>> = _openForms

    fun updateCustomerName(name: String) {
        _formState.update { it.copy(name = name) }
    }

    fun updateCustomerPhone(phone: String) {
        _formState.update { it.copy(phone = phone) }
    }

    fun selectProduct(productId: String) {
        _formState.update { it.copy(selectedProductId = productId) }
    }

    fun toggleFormVisibility(productId: String) {
        _openForms.update { current ->
            if (current.contains(productId)) emptySet()
            else setOf(productId) // only one form open at a time
        }
    }

    fun openForm(productId: String) {
        _openForms.value = setOf(productId)
        _formState.update { it.copy(selectedProductId = UUID.randomUUID().toString()) }
        _formState.update { it.copy(selectedProductId = productId) }
    }

    fun isFormOpen(productId: String): Boolean = _openForms.value.contains(productId)

    fun addProduct(type: ProductType) {
        val newEntry = ProductEntry(
            type = type,
            productOwnerName = _formState.value.name
        )
        _formState.update {
            it.copy(products = it.products + newEntry, selectedProductId = newEntry.id)
        }
        _openForms.value = setOf(newEntry.id)
    }

    fun addNewProduct() {
        val dummyType = ProductType.allTypes.firstOrNull() ?: return
        val newEntry = ProductEntry(
            id = UUID.randomUUID().toString(),
            type = dummyType,
            productOwnerName = _formState.value.name
        )
        _formState.update {
            it.copy(products = it.products + newEntry, selectedProductId = newEntry.id)
        }
        _openForms.value = setOf(newEntry.id)
    }

    fun removeProduct(productId: String) {
        _formState.update {
            val updatedList = it.products.filterNot { product -> product.id == productId }
            val newSelected = if (it.selectedProductId == productId) null else it.selectedProductId
            it.copy(products = updatedList, selectedProductId = newSelected)
        }
        editingBuffer.remove(productId)
        _openForms.update { it - productId }
    }

    fun updateProductOwnerName(productId: String, newName: String) {
        _formState.update {
            val updated = it.products.map {
                if (it.id == productId) it.copy(productOwnerName = newName) else it
            }
            it.copy(products = updated)
        }
    }

    fun saveProductFormData(productId: String, formData: ProductFormData) {
        _formState.update {
            val updated = it.products.map {
                if (it.id == productId) it.copy(formData = formData, isSaved = true) else it
            }
            it.copy(products = updated)
        }
        editingBuffer.remove(productId)
        _openForms.update { it - productId }
    }

    fun hasUnsavedChanges(product: ProductEntry, buffer: ProductFormData?): Boolean {
        return product.isSaved && product.formData != buffer
    }

    fun getEditingProductData(product: ProductEntry): ProductFormData? {
        return editingBuffer[product.id] ?: product.formData
    }

    fun updateEditingBuffer(productId: String, formData: ProductFormData) {
        editingBuffer[productId] = formData
    }

    fun clearForm() {
        _formState.value = CustomerFormState()
        editingBuffer.clear()
        _openForms.value = emptySet()
    }

    fun serializeToJson(): String {
        return "TODO: JSON representation"
    }
}
