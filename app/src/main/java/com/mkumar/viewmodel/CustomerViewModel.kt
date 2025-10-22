package com.mkumar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mkumar.data.CustomerFormState
import com.mkumar.data.ProductEntry
import com.mkumar.data.ProductFormData
import com.mkumar.data.ProductType
import com.mkumar.data.local.entities.CustomerEntity
import com.mkumar.data.repository.CustomerRepository
import com.mkumar.data.repository.OrderDraft
import com.mkumar.data.repository.OrderItemInput
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.time.Clock
import java.util.UUID
import javax.inject.Inject
import kotlin.time.ExperimentalTime

@HiltViewModel
class CustomerViewModel @OptIn(ExperimentalTime::class)
@Inject constructor(
    private val repository: CustomerRepository,
    private val clock: Clock
) : ViewModel() {

    private val _customers = MutableStateFlow<List<CustomerFormState>>(emptyList())
    private val uiStateByCustomer = MutableStateFlow<Map<String, CustomerFormState>>(emptyMap())
    val customersTemp: StateFlow<List<CustomerFormState>> = _customers

    private val _currentCustomerId = MutableStateFlow<String?>(null)
    val currentCustomerId: StateFlow<String?> = _currentCustomerId

    private val _formState = MutableStateFlow(CustomerFormState())

    val editingBuffer: MutableMap<String, MutableMap<String, ProductFormData?>> = mutableMapOf()
    private val _openForms = MutableStateFlow<Map<String, Set<String>>>(emptyMap())
    val openForms: StateFlow<Map<String, Set<String>>> = _openForms

    val customersUi: StateFlow<List<CustomerFormState>> =
        combine(repository.getAllCustomersFlow(), uiStateByCustomer) { entities, cache ->
            entities
                .map { e ->
                    // Reuse any in-memory products/buffers if present
                    cache[e.id]?.copy(
                        // keep products/selectedProductId from cache
                        name = e.name,
                        phone = e.phone,
                    ) ?: CustomerFormState(
                        id = e.id,
                        name = e.name,
                        phone = e.phone,
                        products = emptyList(),          // no products from DB here
                        selectedProductId = null,
                    )
                }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

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

    fun createOrUpdateCustomerCard(name: String, phone: String, email: String? = null) {
        val customer = CustomerEntity(
            id = UUID.randomUUID().toString(), // or ULID
            name = name.trim(),
            phone = phone.trim(),
        )
        viewModelScope.launch {
            // If you want "update if same phone exists", do a DAO lookup and reuse ID.
            repository.upsertCustomerOnly(customer)
            // optional: update search index here if you wired SearchDao
        }
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

    @OptIn(ExperimentalTime::class)
    fun saveOrderForCustomer(
        customerId: String,
        uiItems: List<UiProductLine>,
        note: String? = null,
        orderId: String? = null,
        discount: Double = 0.0,
        tax: Double = 0.0
    ) {
        val computed = computeTotals(uiItems, discount, tax)
        val draft = OrderDraft(
            orderId = orderId,
            note = note,
            items = uiItems.map {
                OrderItemInput(
                    sku = it.sku,
                    name = it.name.trim(),
                    quantity = it.quantity,
                    unitPrice = it.unitPrice,
                    lineTotal = it.quantity * it.unitPrice
                )
            }
        )

        viewModelScope.launch {
            if (orderId == null) {
                repository.createOrder(customerId, draft)     // new order
            } else {
                // If you support editing an order, reuse repository.saveCustomer(form, draft)
                // or add an update method. For now, simply replace by ID using repo.saveCustomer with a form stub:
                repository.saveCustomer(
                    form = CustomerFormState(
                        id = customerId,
                        name = "",                 // not changing here
                        products = emptyList(),    // pricing deferred; we pass draft
                    ),
                    orderDraft = draft
                )
            }
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

    // --- Helpers ---
    private data class Totals(val subTotal: Double, val discount: Double, val tax: Double, val grandTotal: Double)

    private fun computeTotals(lines: List<UiProductLine>, discount: Double, tax: Double): Totals {
        val sub = lines.sumOf { it.quantity * it.unitPrice }
        val grand = (sub - discount) + tax
        return Totals(sub, discount, tax, grand)
    }
}

data class UiProductLine(
    val sku: String? = null,
    val name: String,
    val quantity: Int,
    val unitPrice: Double
)
