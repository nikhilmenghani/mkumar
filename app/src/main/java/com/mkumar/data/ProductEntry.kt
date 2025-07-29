package com.mkumar.data

import java.util.UUID

data class ProductEntry(
    val id: String = UUID.randomUUID().toString(),
    val type: ProductType,
    val productOwnerName: String = "",
    val formData: ProductFormData? = null,
    val isSaved: Boolean = false
)
