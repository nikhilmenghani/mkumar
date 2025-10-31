// com.mkumar.domain.pricing.PricingInteractor.kt
package com.mkumar.domain.pricing

import com.mkumar.data.db.dao.OrderDao
import com.mkumar.data.db.dao.OrderItemDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PricingInteractor @Inject constructor(
    private val orderDao: OrderDao,
    private val orderItemDao: OrderItemDao,
    private val pricing: PricingService
) {
    /** Call this after any item/add/remove/edit or when adjusted/advance changes. */
    suspend fun recomputeOrderTotals(orderId: String) = withContext(Dispatchers.IO) {
        val order = orderDao.getById(orderId) ?: return@withContext
        val items = orderItemDao.getItemsForOrder(orderId)
        val result = pricing.price(buildPricingInput(order, items))
        orderDao.upsert(order.withTotals(result))
    }
}
