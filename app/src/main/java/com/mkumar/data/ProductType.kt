// File: app/src/main/java/com/mkumar/data/ProductType.kt
package com.mkumar.data

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
sealed class ProductType {
    abstract val label: String

    @Serializable
    @SerialName("Frame")
    object Frame : ProductType() {
        override val label: String = "Frame"
    }

    @Serializable
    @SerialName("Lens")
    object Lens : ProductType() {
        override val label: String = "Lens"
    }

    @Serializable
    @SerialName("ContactLens")
    object ContactLens : ProductType() {
        override val label: String = "Contact Lens"
    }

    companion object {
        val allTypes = listOf(Frame, Lens, ContactLens)

        fun fromLabel(label: String): ProductType =
            allTypes.find { it.label == label } ?: Lens

    }
}
