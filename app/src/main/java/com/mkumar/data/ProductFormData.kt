package com.mkumar.data

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type") 
sealed class ProductFormData {

    abstract val unitPrice: Int
    abstract val quantity: Int
    abstract val discountPct: Int
    abstract val total: Int
    abstract val productDescription: String
    abstract val productOwner: String

    @Serializable
    @SerialName("FrameData")
    data class FrameData(
        override val productDescription: String = "",
        override val productOwner: String = "",
        override val unitPrice: Int = 0,
        override val quantity: Int = 1,
        override val discountPct: Int = 0,
        override val total: Int = 0
    ) : ProductFormData()

    @Serializable
    @SerialName("LensData")
    data class LensData(
        override val productDescription: String = "",
        override val productOwner: String = "",
        override val unitPrice: Int = 0,
        override val quantity: Int = 1,
        override val discountPct: Int = 0,
        override val total: Int = 0
    ) : ProductFormData()

    @Serializable
    @SerialName("ContactLensData")
    data class ContactLensData(
        val rightSph: String = "",
        val rightCyl: String = "",
        val rightAxis: String = "",
        val rightAdd: String = "",
        val leftSph: String = "",
        val leftCyl: String = "",
        val leftAxis: String = "",
        val leftAdd: String = "",
        override val productDescription: String = "",
        override val productOwner: String = "",
        override val unitPrice: Int = 0,
        override val quantity: Int = 1,
        override val discountPct: Int = 0,
        override val total: Int = 0
    ) : ProductFormData()

    @Serializable
    @SerialName("GeneralProductData")
    data class GeneralProductData(
        val productType: String = "",
        override val productDescription: String = "",
        override val productOwner: String = "",
        override val unitPrice: Int = 0,
        override val quantity: Int = 1,
        override val discountPct: Int = 0,
        override val total: Int = 0
    ) : ProductFormData()
}
