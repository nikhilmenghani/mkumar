package com.mkumar.data

@kotlinx.serialization.Serializable
data class CustomerFormState(
    val name: String = "",
    val phone: String = "",
    val products: List<ProductEntry> = emptyList(),
    val selectedProductId: String? = null
)
