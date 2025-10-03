package com.mkumar.data

import java.util.UUID

@kotlinx.serialization.Serializable
data class ProductEntry(
    val id: String = UUID.randomUUID().toString(),
    val productType: ProductType,
    val productOwnerName: String = "",
    val formData: ProductFormData? = null,
    val isSaved: Boolean = false
)
