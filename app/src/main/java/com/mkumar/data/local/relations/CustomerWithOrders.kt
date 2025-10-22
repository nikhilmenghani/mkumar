package com.mkumar.data.local.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.mkumar.data.local.entities.CustomerEntity
import com.mkumar.data.local.entities.OrderEntity

data class CustomerWithOrders(
    @Embedded val customer: CustomerEntity,
    @Relation(parentColumn = "id", entityColumn = "customerId")
    val orders: List<OrderEntity>
)
