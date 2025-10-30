package com.mkumar.data

import kotlinx.serialization.json.Json
import java.util.UUID

@kotlinx.serialization.Serializable
data class ProductEntry(
    val id: String = UUID.randomUUID().toString(),
    val productType: ProductType,
    val productOwnerName: String = "",
    val productDescription: String = "",
    val formData: ProductFormData? = null,
    val quantity: Int = 1,
    val unitPrice: Int = 0,
    val discountPercentage: Int = 0,
    val finalTotal: Int = 0,
    val isSaved: Boolean = false
) {
    fun serializeFormData(): String? {
        return formData?.let { Json.encodeToString(it) }
    }

    companion object {
        fun deserializeFormData(json: String?): ProductFormData? {
            return json?.let { Json.decodeFromString<ProductFormData>(it) }
        }
    }
}
