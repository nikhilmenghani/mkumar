package com.mkumar.data

import kotlinx.serialization.json.Json
import java.util.UUID

@kotlinx.serialization.Serializable
data class ProductEntry(
    val id: String = UUID.randomUUID().toString(),
    val productType: ProductType,
    val productOwnerName: String = "",
    val formData: ProductFormData? = null,
    val quantity: Int = 1,
    val unitPriceMinor: Long = 0L,
    val discountPercentage: Double = 0.0,
    val finalTotal: Long = 0L,
    val isSaved: Boolean = false
) {
    fun serializeFormData(): String? {
        return formData?.let { Json.encodeToString(it) }
    }

    fun calculateFinalTotal(): Long {
        val discountedPrice = unitPriceMinor * quantity * (1 - discountPercentage / 100)
        return discountedPrice.toLong()
    }

    companion object {
        fun deserializeFormData(json: String?): ProductFormData? {
            return json?.let { Json.decodeFromString<ProductFormData>(it) }
        }
    }
}
