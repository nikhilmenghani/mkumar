package com.mkumar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mkumar.common.constant.CustomerDetailsConstants
import com.mkumar.data.PreferencesManager
import com.mkumar.domain.invoice.InvoiceManager
import com.mkumar.model.CustomerDetailsEffect
import com.mkumar.network.VersionFetcher
import com.mkumar.repository.CustomerRepository
import com.mkumar.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.ExperimentalTime

@HiltViewModel
class CustomerViewModel @OptIn(ExperimentalTime::class)
@Inject constructor(
    private val repository: CustomerRepository,
    private val invoiceManager: InvoiceManager,
    private val orderRepo: OrderRepository,
    private val preferencesManager: PreferencesManager,
    private val versionFetcher: VersionFetcher
) : ViewModel() {

    /* -------------------- SORT STATE -------------------- */

    private val _orderSortBy = MutableStateFlow("Invoice")
    private val _orderSortAsc = MutableStateFlow(false)

    val orderSortBy: StateFlow<String> = _orderSortBy.asStateFlow()
    val orderSortAsc: StateFlow<Boolean> = _orderSortAsc.asStateFlow()

    fun setOrderSortBy(sortBy: String) {
        _orderSortBy.value = sortBy
    }

    fun setOrderSortAsc(asc: Boolean) {
        _orderSortAsc.value = asc
    }

    /* -------------------- HOME UI -------------------- */

    private val _homeUi = MutableStateFlow(HomeUiState())
    val homeUi: StateFlow<HomeUiState> = _homeUi.asStateFlow()

    /**
     * Call ONCE from HomeScreen (LaunchedEffect).
     * This restores old behaviour where update icon appeared immediately.
     */
    fun bootstrap(currentVersion: String) {
        if (_homeUi.value.hasBootstrapped) return

        _homeUi.update {
            it.copy(
                currentVersion = currentVersion,
                hasBootstrapped = true
            )
        }
        checkVersion()
    }

    fun checkVersion() {
        viewModelScope.launch {
            _homeUi.update { it.copy(isChecking = true) }

            val latest = versionFetcher.fetchLatestVersion()

            _homeUi.update { state ->
                state.copy(
                    latestVersion = latest,
                    isLatest = (latest == state.currentVersion) || (latest == "Unknown"),
                    isChecking = false
                )
            }
        }
    }

    fun setDownloading(downloading: Boolean) {
        _homeUi.update { it.copy(isDownloading = downloading) }
    }

    /* -------------------- EFFECTS -------------------- */

    private val _effects = MutableSharedFlow<CustomerDetailsEffect>(extraBufferCapacity = 64)
    val effects: Flow<CustomerDetailsEffect> = _effects.asSharedFlow()

    /* -------------------- RECENT ORDERS -------------------- */

    @OptIn(ExperimentalCoroutinesApi::class)
    val recentOrders = combine(
        _orderSortBy,
        _orderSortAsc
    ) { sortBy, asc ->
        sortBy to asc
    }.flatMapLatest { (sortBy, asc) ->
        repository.getRecentOrders(
            limit = 10,
            sortBy = sortBy,
            ascending = asc
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = emptyList()
    )

    /* -------------------- CUSTOMER / ORDER -------------------- */

    fun removeCustomer(customerID: String) {
        viewModelScope.launch {
            repository.deleteById(customerID)
        }
    }

    fun deleteOrder(orderId: String) {
        viewModelScope.launch {
            try {
                orderRepo.delete(orderId)
                emitMessage("Order deleted.")
            } catch (t: Throwable) {
                emitMessage("Failed to delete order: ${t.message}")
            }
        }
    }

    /* -------------------- INVOICE -------------------- */

    fun getInvoicePrefix(): String =
        preferencesManager.invoicePrefs.invoicePrefix

    fun humanReadableInvoiceLocation(orderId: String, invoiceNumber: String): String {
        val fileName = CustomerDetailsConstants.getInvoiceFileName(
            orderId = orderId,
            invoiceNumber = invoiceNumber,
            invoicePrefix = preferencesManager.invoicePrefs.invoicePrefix,
            invoiceDateFormatOrdinal = preferencesManager.invoicePrefs.invoiceDateFormat
        ) + ".pdf"

        return "Files > Downloads > Documents > MKumar > Invoices > $fileName"
    }

    fun shareInvoice(orderId: String, invoiceNumber: String) {
        viewModelScope.launch {
            _effects.emit(CustomerDetailsEffect.ShowMessage("Creating invoice…"))
            when (val r = invoiceManager.createInvoice(orderId, invoiceNumber)) {
                is InvoiceManager.InvoiceResult.Success ->
                    _effects.emit(CustomerDetailsEffect.ShareInvoice(orderId, r.uri))
                is InvoiceManager.InvoiceResult.NotFound ->
                    _effects.emit(CustomerDetailsEffect.ShowMessage(r.message))
                is InvoiceManager.InvoiceResult.Error ->
                    _effects.emit(CustomerDetailsEffect.ShowMessage("Failed: ${r.throwable.message}"))
            }
        }
    }

    fun viewInvoice(orderId: String, invoiceNumber: String) {
        viewModelScope.launch {
            _effects.emit(CustomerDetailsEffect.ShowMessage("Creating invoice…"))
            when (val r = invoiceManager.createInvoice(orderId, invoiceNumber)) {
                is InvoiceManager.InvoiceResult.Success ->
                    _effects.emit(CustomerDetailsEffect.ViewInvoice(orderId, invoiceNumber, r.uri))
                is InvoiceManager.InvoiceResult.NotFound ->
                    _effects.emit(CustomerDetailsEffect.ShowMessage(r.message))
                is InvoiceManager.InvoiceResult.Error ->
                    _effects.emit(CustomerDetailsEffect.ShowMessage("Failed: ${r.throwable.message}"))
            }
        }
    }

    private fun emitMessage(msg: String) {
        _effects.tryEmit(CustomerDetailsEffect.ShowMessage(msg))
    }
}