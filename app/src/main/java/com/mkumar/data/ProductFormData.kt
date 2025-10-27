package com.mkumar.data

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type") 
sealed class ProductFormData {

    abstract val unitPrice: Long
    abstract val quantity: Int
    abstract val discountPct: Int
    abstract val total: Int

    @Serializable
    @SerialName("FrameData")
    data class FrameData(
        val brand: String = "",
        val color: String = "",
        val size: String = "",
        override val unitPrice: Long = 0L,
        override val quantity: Int = 1,
        override val discountPct: Int = 0,
        override val total: Int = 0
    ) : ProductFormData()

    @Serializable
    @SerialName("LensData")
    data class LensData(
        val leftSphere: String = "",
        val leftAxis: String = "",
        val rightSphere: String = "",
        val rightAxis: String = "",
        override val unitPrice: Long = 0L,
        override val quantity: Int = 1,
        override val discountPct: Int = 0,
        override val total: Int = 0
    ) : ProductFormData()

    @Serializable
    @SerialName("ContactLensData")
    data class ContactLensData(
        val power: String = "",
        val duration: String = "",
        override val unitPrice: Long = 0L,
        override val quantity: Int = 1,
        override val discountPct: Int = 0,
        override val total: Int = 0
    ) : ProductFormData()
}
