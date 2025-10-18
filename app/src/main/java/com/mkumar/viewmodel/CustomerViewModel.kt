package com.mkumar.viewmodel

import androidx.lifecycle.ViewModel
import com.mkumar.data.CustomerFormState
import com.mkumar.data.ProductEntry
import com.mkumar.data.ProductFormData
import com.mkumar.data.ProductType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.Base64
import java.util.UUID

class CustomerViewModel : ViewModel() {

    private val _customers = MutableStateFlow<List<CustomerFormState>>(emptyList())
    val customers: StateFlow<List<CustomerFormState>> = _customers

    private val _currentCustomerId = MutableStateFlow<String?>(null)
    val currentCustomerId: StateFlow<String?> = _currentCustomerId

    private val _formState = MutableStateFlow(CustomerFormState())
    val formState: StateFlow<CustomerFormState> = _formState

    val editingBuffer: MutableMap<String, MutableMap<String, ProductFormData?>> = mutableMapOf()
    private val _openForms = MutableStateFlow<Map<String, Set<String>>>(emptyMap())
    val openForms: StateFlow<Map<String, Set<String>>> = _openForms

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

    fun listCustomers() : List<CustomerFormState> {
        // return the list of all the customers added so UI can display them in a list
        return _customers.value
    }

    fun updateCustomerName(customerId: String, name: String) {
        _customers.update { list ->
            list.map { if (it.id == customerId) it.copy(name = name) else it }
        }
    }

    fun updateCustomerPhone(customerId: String, phone: String) {
        _customers.update { list ->
            list.map { if (it.id == customerId) it.copy(phone = phone) else it }
        }
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

    fun toggleFormVisibility(customerId: String, productId: String) {
        _openForms.update { map ->
            val current = map[customerId].orEmpty()
            val next = if (current.contains(productId)) emptySet() else setOf(productId) // one open at a time per customer
            map + (customerId to next)
        }
    }

    fun openForm(customerId: String, productId: String) {
        _openForms.update { it + (customerId to setOf(productId)) }
        // keep selectedProductId stable (no random UUID overwrites)
        selectProduct(customerId, productId)
    }

    fun isFormOpen(customerId: String, productId: String): Boolean =
        _openForms.value[customerId]?.contains(productId) == true

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

    fun addNewProduct(customerId: String) {
        val dummyType = ProductType.allTypes.firstOrNull() ?: return
        val newEntry = ProductEntry(
            id = UUID.randomUUID().toString(),
            productType = dummyType,
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

    fun clearAll() {
        _customers.value = emptyList()
        _currentCustomerId.value = null
        editingBuffer.clear()
        _openForms.value = emptyMap()
    }

    fun serializeToJson(includeUnsavedEdits: Boolean = true): String {
        val merged = _customers.value.map { c ->
            val mergedProducts = c.products.map { p ->
                val eff = if (includeUnsavedEdits)
                    editingBuffer[c.id]?.get(p.id) ?: p.formData
                else p.formData
                p.copy(formData = eff)
            }
            c.copy(products = mergedProducts)
        }
        val json = Json { encodeDefaults = true; ignoreUnknownKeys = true; classDiscriminator = "type"; prettyPrint = true }
        return json.encodeToString(ListSerializer(CustomerFormState.serializer()), merged)
    }

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

    fun syncCustomerToGitHub(customerId: String, githubToken: String, repo: String, owner: String) {
        val customer = _customers.value.find { it.id == customerId } ?: return
        val json = serializeCustomer(customerId) ?: return
        val phone = customer.phone
        val fileName = "$phone.json"
        val apiUrl = "https://api.github.com/repos/$owner/$repo/contents/$fileName"
        val client = OkHttpClient()

        // Check if file exists
        val requestGet = Request.Builder()
            .url(apiUrl)
            .header("Authorization", "token $githubToken")
            .build()
        val responseGet = client.newCall(requestGet).execute()
        val sha = if (responseGet.code == 200) {
            val body = responseGet.body.string()
            Regex("\"sha\":\\s*\"([^\"]+)\"").find(body)?.groupValues?.get(1)
        } else null

        // Prepare request body
        val encodedContent = Base64.getEncoder().encodeToString(json.toByteArray())
        val jsonBody = if (sha != null) {
            """
        {
          "message": "Update customer $phone",
          "content": "$encodedContent",
          "sha": "$sha"
        }
        """
        } else {
            """
        {
          "message": "Add customer $phone",
          "content": "$encodedContent"
        }
        """
        }

        val requestPut = Request.Builder()
            .url(apiUrl)
            .header("Authorization", "token $githubToken")
            .put(jsonBody.toRequestBody("application/json".toMediaTypeOrNull()))
            .build()
        client.newCall(requestPut).execute().close()
    }

//    fun serializeToJson(includeUnsavedEdits: Boolean = true): String {
//        // Snapshot current state
//        val state = _formState.value
//
//        // Merge in-progress edits if requested
//        val mergedProducts = state.products.map { product ->
//            val effectiveFormData =
//                if (includeUnsavedEdits) editingBuffer[product.id] ?: product.formData
//                else product.formData
//
//            // Return a non-mutating copy with the effective formData
//            product.copy(formData = effectiveFormData)
//        }
//
//        val snapshot = state.copy(products = mergedProducts)
//
//        // Configure JSON (tweak as needed)
//        val json = Json {
//            encodeDefaults = true
//            ignoreUnknownKeys = true
//            classDiscriminator = "type" // helpful for sealed ProductFormData
//            prettyPrint = true
//        }
//
//        return json.encodeToString(snapshot)
//    }

    fun loadFromJson(text: String) {
        val json = Json {
            encodeDefaults = true
            ignoreUnknownKeys = true
            classDiscriminator = "type" // helpful for sealed ProductFormData
            prettyPrint = true
        }
        val decoded = json.decodeFromString(CustomerFormState.serializer(), text)
        _formState.value = decoded
        editingBuffer.clear() // optional: drop stale edits after loading
    }
}
