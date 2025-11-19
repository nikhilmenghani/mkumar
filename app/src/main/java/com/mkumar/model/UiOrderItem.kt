package com.mkumar.model

import com.mkumar.data.ProductFormData
import kotlinx.serialization.json.Json
import java.util.UUID

/**
 * Item model used inside OrderEditor + CustomerDetails screen.
 */
data class UiOrderItem(
    val id: String = UUID.randomUUID().toString(),
    val productType: ProductType,
    val productDescription: String = "",
    val formData: ProductFormData? = null,
    val name: String,
    val quantity: Int,
    val unitPrice: Int,
    val discountPercentage: Int,
    val finalTotal: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
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