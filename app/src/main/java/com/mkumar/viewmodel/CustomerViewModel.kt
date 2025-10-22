package com.mkumar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mkumar.data.CustomerFormState
import com.mkumar.data.CustomerRepository
import com.mkumar.data.ProductEntry
import com.mkumar.data.ProductFormData
import com.mkumar.data.ProductType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerViewModel @Inject constructor(
    private val repo: CustomerRepository,
    private val json: Json
) : ViewModel() {

    private val _customers = MutableStateFlow<List<CustomerFormState>>(emptyList())
    val customers: StateFlow<List<CustomerFormState>> = _customers

    private val _currentCustomerId = MutableStateFlow<String?>(null)
    val currentCustomerId: StateFlow<String?> = _currentCustomerId

    private val _formState = MutableStateFlow(CustomerFormState())

    val editingBuffer: MutableMap<String, MutableMap<String, ProductFormData?>> = mutableMapOf()
    private val _openForms = MutableStateFlow<Map<String, Set<String>>>(emptyMap())
    val openForms: StateFlow<Map<String, Set<String>>> = _openForms

    val dbCustomers = repo.observeCustomers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addCustomer(name: String, phone: String) {
        val newCustomer = CustomerFormState(
            name = name,
            phone = phone,
            products = emptyList(),
            selectedProductId = null
        )
        _customers.update { it + newCustomer }
        _currentCustomerId.value = newCustomer.id
    }

    fun selectCustomer(customerID: String?) {
        _currentCustomerId.value = customerID
        _formState.value = _customers.value.find { it.id == customerID }
            ?: CustomerFormState()
    }

    fun removeCustomer(customerId: String) {
        _customers.update { it.filterNot { c -> c.id == customerId } }
        editingBuffer.remove(customerId)
        _openForms.update { it - customerId }
        if (_currentCustomerId.value == customerId) {
            _currentCustomerId.value = _customers.value.lastOrNull()?.id
        }
    }

    fun selectProduct(customerId: String, productId: String) {
        _customers.update { list ->
            list.map { c ->
                if (c.id == customerId) c.copy(selectedProductId = productId) else c
            }
        }
    }

    fun openForm(customerId: String, productId: String) {
        _openForms.update { it + (customerId to setOf(productId)) }
        // keep selectedProductId stable (no random UUID overwrites)
        selectProduct(customerId, productId)
    }

    fun addProduct(customerId: String, type: ProductType) {
        val newEntry = ProductEntry(
            productType = type,
            productOwnerName = getCustomer(customerId)?.name.orEmpty()
        )
        _customers.update { list ->
            list.map { c ->
                if (c.id != customerId) c
                else c.copy(
                    products = c.products + newEntry,
                    selectedProductId = newEntry.id
                )
            }
        }
        // open the newly added product form
        _openForms.update { it + (customerId to setOf(newEntry.id)) }
    }

    fun removeProduct(customerId: String, productId: String) {
        _customers.update { list ->
            list.map { c ->
                if (c.id != customerId) c
                else {
                    val updated = c.products.filterNot { it.id == productId }
                    val newSelected = if (c.selectedProductId == productId) null else c.selectedProductId
                    c.copy(products = updated, selectedProductId = newSelected)
                }
            }
        }
        editingBuffer[customerId]?.remove(productId)
        _openForms.update { it + (customerId to (_openForms.value[customerId].orEmpty() - productId)) }
    }

    fun updateProductOwnerName(customerId: String, productId: String, newName: String) {
        _customers.update { list ->
            list.map { c ->
                if (c.id != customerId) c
                else c.copy(
                    products = c.products.map { p ->
                        if (p.id == productId) p.copy(productOwnerName = newName) else p
                    }
                )
            }
        }
    }

    fun saveProductFormData(customerId: String, productId: String, formData: ProductFormData) {
        _customers.update { list ->
            list.map { c ->
                if (c.id != customerId) c
                else c.copy(
                    products = c.products.map { p ->
                        if (p.id == productId) p.copy(formData = formData, isSaved = true) else p
                    }
                )
            }
        }
        editingBuffer[customerId]?.remove(productId)
        _openForms.update { it + (customerId to (_openForms.value[customerId].orEmpty() - productId)) }
    }

    fun saveCurrentCustomerToDb(discountMinor: Long = 0L, includeUnsavedEdits: Boolean = true) {
        val id = currentCustomerId.value ?: return
        val snapshot = serializeCustomer(id, includeUnsavedEdits)
        val form = json.decodeFromString(CustomerFormState.serializer(), snapshot)
        viewModelScope.launch {
            repo.upsertCustomerWithOrderSnapshot(form)
        }
    }

    fun hasUnsavedChanges(customerId: String, product: ProductEntry, buffer: ProductFormData?): Boolean {
        return product.isSaved && product.formData != buffer
    }

    fun getEditingProductData(customerId: String, product: ProductEntry): ProductFormData? {
        return editingBuffer[customerId]?.get(product.id) ?: product.formData
    }

    fun updateEditingBuffer(customerId: String, productId: String, formData: ProductFormData) {
        val map = editingBuffer.getOrPut(customerId) { mutableMapOf() }
        map[productId] = formData
    }

    private fun getCustomer(customerId: String): CustomerFormState? =
        _customers.value.firstOrNull { it.id == customerId }

    fun serializeCustomer(customerId: String, includeUnsavedEdits: Boolean = true): String {
        val customer = _customers.value.find { it.id == customerId } ?: return ""
        val mergedProducts = customer.products.map { p ->
            val eff = if (includeUnsavedEdits)
                editingBuffer[customer.id]?.get(p.id) ?: p.formData
            else p.formData
            p.copy(formData = eff)
        }
        val snapshot = customer.copy(products = mergedProducts)
        val json = Json { encodeDefaults = true; ignoreUnknownKeys = true; classDiscriminator = "type"; prettyPrint = true }
        return json.encodeToString(CustomerFormState.serializer(), snapshot)
    }
}
