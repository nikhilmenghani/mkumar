package com.mkumar.data

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type") 
sealed class ProductFormData {

    @Serializable
    @SerialName("FrameData")
    data class FrameData(
        val brand: String = "",
        val color: String = "",
        val size: String = ""
    ) : ProductFormData()

    @Serializable
    @SerialName("LensData")
    data class LensData(
        val leftSphere: String = "",
        val leftAxis: String = "",
        val rightSphere: String = "",
        val rightAxis: String = ""
    ) : ProductFormData()

    @Serializable
    @SerialName("ContactLensData")
    data class ContactLensData(
        val power: String = "",
        val duration: String = ""
    ) : ProductFormData()
}
