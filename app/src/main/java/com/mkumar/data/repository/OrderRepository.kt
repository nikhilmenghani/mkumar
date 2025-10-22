package com.mkumar.data.repository

import com.mkumar.data.OrderSummaryDomain
import javax.inject.Inject

class OrderRepository @Inject constructor() {
    suspend fun ordersForCustomer(customerId: String): List<OrderSummaryDomain> {
        // Implement logic here
        return emptyList()
    }

    suspend fun createDraftOrder(customerId: String): String {
        // Implement logic here
        return ""
    }
}