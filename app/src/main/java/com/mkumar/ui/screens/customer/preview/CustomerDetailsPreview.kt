//package com.mkumar.ui.screens.customer.preview
//
//
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.navigation.compose.rememberNavController
//import com.mkumar.ui.screens.customer.CustomerDetailsScreen
//import com.mkumar.ui.screens.customer.model.*
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import java.time.Instant
//
//
//private class PreviewVM : CustomerDetailsViewModel {
//    private val _ui = MutableStateFlow(
//        CustomerDetailsUiState(
//            header = CustomerHeaderUi(
//                id = "c1",
//                name = "Aarti Parpiyani",
//                phoneFormatted = "+1 416-555-0100",
//                joinedAt = Instant.now().minusSeconds(86400L * 100),
//                totalOrders = 3,
//                totalSpent = 12345
//            ),
//            orders = listOf(
//                OrderRowUi("o1", Instant.now(), "1× Lens, 1× Frame", 4500, isQueued = true, isSynced = false, hasInvoice = false),
//                OrderRowUi("o2", Instant.now().minusSeconds(86400), "2× Contact Lens", 3200, isQueued = false, isSynced = true, hasInvoice = true),
//            ),
//            newOrder = NewOrderUi(selectedType = ProductType.LENS)
//        )
//    )
//    override val ui: StateFlow<CustomerDetailsUiState> = _ui
//
//
//    override fun startNewOrder() {}
//    override fun onNewOrderIntent(intent: NewOrderIntent) {
//        _ui.value = _ui.value.copy(newOrder = when (intent) {
//            is NewOrderIntent.SelectType -> _ui.value.newOrder.copy(selectedType = intent.type)
//            is NewOrderIntent.LensChanged -> _ui.value.newOrder.copy(lens = intent.state)
//            is NewOrderIntent.FrameChanged -> _ui.value.newOrder.copy(frame = intent.state)
//            is NewOrderIntent.ContactLensChanged -> _ui.value.newOrder.copy(contactLens = intent.state)
//            is NewOrderIntent.Save -> _ui.value.newOrder
//        })
//    }
//    override suspend fun createOrder(): CreateOrderResult = CreateOrderResult.Success("preview")
//    override fun onFilterChange(newFilter: OrderFilterUi) {}
//    override fun refreshFromCloud() {}
//    override fun onOrderAction(action: OrderRowAction) {}
//}
//
//
//@Preview(showBackground = true)
//@Composable
//private fun CustomerDetailsPreview() {
//    CustomerDetailsScreen(
//        navController = rememberNavController(),
//        viewModel = PreviewVM()
//    )
//}