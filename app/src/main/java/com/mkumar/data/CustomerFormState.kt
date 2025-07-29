package com.mkumar.data

data class CustomerFormState(
    val name: String = "",
    val phone: String = "",
    val products: List<ProductEntry> = emptyList(),
    val selectedProductId: String? = null
)
