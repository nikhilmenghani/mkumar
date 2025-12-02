package com.mkumar.repository.helpers

import com.mkumar.data.db.entities.OrderEntity
import com.mkumar.data.db.entities.toSyncDto
import com.mkumar.repository.SyncRepository
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderSyncHelper @Inject constructor(
    private val syncRepository: SyncRepository,
    private val json: Json
) {

    suspend fun enqueueOrderUpsert(order: OrderEntity, items: List<com.mkumar.data.db.entities.OrderItemEntity>) {
        val dto = order.toSyncDto(items)
        val payload = json.encodeToString(dto)

        syncRepository.enqueueOperation(
            type = "ORDER_UPSERT",
            payloadJson = payload,
            entityId = order.id,
            cloudPath = "customers/${order.customerId}/orders/${order.id}.json",
            priority = 5,
            opUpdatedAt = order.updatedAt
        )
    }
}
