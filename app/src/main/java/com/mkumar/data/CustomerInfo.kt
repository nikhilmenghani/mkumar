package com.mkumar.data

import kotlinx.serialization.Serializable

@Serializable
data class CustomerInfo(
    val name: String = "",
    val phoneNumber: String = "",
    val email: String = ""
)

@Serializable
data class PurchasedItem(
    val productType: String, // e.g. "Lens", "Frame", etc.
    val details: String = "", // e.g. lens prescription or frame color
    val quantity: Int = 1
)

// This object holds everything the user enters in the multi-step form.
@Serializable
data class CustomerOrder(
    val customerInfo: CustomerInfo = CustomerInfo(),
    val items: List<PurchasedItem> = emptyList()
)