package com.mkumar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mkumar.data.CustomerFormState
import com.mkumar.data.ProductFormData
import com.mkumar.data.db.entities.CustomerEntity
import com.mkumar.repository.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import kotlin.time.ExperimentalTime

@HiltViewModel
class CustomerViewModel @OptIn(ExperimentalTime::class)
@Inject constructor(
    private val repository: CustomerRepository
) : ViewModel() {

    private val _customers = MutableStateFlow<List<CustomerFormState>>(emptyList())
    private val uiStateByCustomer = MutableStateFlow<Map<String, CustomerFormState>>(emptyMap())

    private val _currentCustomerId = MutableStateFlow<String?>(null)
    val currentCustomerId: StateFlow<String?> = _currentCustomerId

    private val _formState = MutableStateFlow(CustomerFormState())

    val recentCustomers = repository.getRecentCustomers(limit = 10)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val recentOrders = repository.getRecentOrders(limit = 10)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())


    val editingBuffer: MutableMap<String, MutableMap<String, ProductFormData?>> = mutableMapOf()
    private val _openForms = MutableStateFlow<Map<String, Set<String>>>(emptyMap())
    val openForms: StateFlow<Map<String, Set<String>>> = _openForms

    val customersUi: StateFlow<List<CustomerFormState>> =
        combine(repository.observeAll(), uiStateByCustomer) { entities, cache ->
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
//                        products = emptyList(),          // no products from DB here
//                        selectedProductId = null,
                    )
                }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun createOrUpdateCustomerCard(name: String, phone: String, email: String? = null): String {
        val customer = CustomerEntity(
            id = UUID.randomUUID().toString(), // or ULID
            name = name.trim(),
            phone = phone.trim(),
        )
        viewModelScope.launch {
            // If you want "update if same phone exists", do a DAO lookup and reuse ID.
            repository.upsert(customer)
            // optional: update search index here if you wired SearchDao
        }
        return customer.id
    }

    fun removeCustomer(customerID: String) {
        viewModelScope.launch {
            repository.deleteById(customerID)
        }
    }

    fun selectCustomer(customerID: String?) {
        _currentCustomerId.value = customerID
        _formState.value = _customers.value.find { it.id == customerID }
            ?: CustomerFormState()
    }

    fun updateCustomer(id: String, name: String, phone: String) {
        viewModelScope.launch {
            repository.upsert(
                CustomerEntity(
                    id = id,                // keep same id to update
                    name = name.trim(),
                    phone = phone.trim()
                )
            )
            // If you maintain an in-memory cache (uiStateByCustomer), reflect it here if needed:
            uiStateByCustomer.value = uiStateByCustomer.value.toMutableMap().apply {
                val current = this[id]
                if (current != null) {
                    put(id, current.copy(name = name.trim(), phone = phone.trim()))
                }
            }
        }
    }

//    fun serializeCustomer(customerId: String, includeUnsavedEdits: Boolean = true): String {
//        val customer = _customers.value.find { it.id == customerId } ?: return ""
//        val mergedProducts = customer.products.map { p ->
//            val eff = if (includeUnsavedEdits)
//                editingBuffer[customer.id]?.get(p.id) ?: p.formData
//            else p.formData
//            p.copy(formData = eff)
//        }
//        val snapshot = customer.copy(products = mergedProducts)
//        val json = Json { encodeDefaults = true; ignoreUnknownKeys = true; classDiscriminator = "type"; prettyPrint = true }
//        return json.encodeToString(CustomerFormState.serializer(), snapshot)
//    }
}