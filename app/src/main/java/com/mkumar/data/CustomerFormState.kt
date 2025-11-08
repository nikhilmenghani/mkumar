package com.mkumar.data

@kotlinx.serialization.Serializable
data class CustomerFormState(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String = "testName",
    val phone: String = "1234567890",
//    val products: List<ProductEntry> = emptyList(),
//    val selectedProductId: String? = null
)
