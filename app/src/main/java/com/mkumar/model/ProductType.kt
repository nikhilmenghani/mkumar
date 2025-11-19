package com.mkumar.model

enum class ProductType { Glass, Frame, Lens, GeneralProduct }

val productTypeDisplayNames = mapOf(
    ProductType.Lens to "Lens",
    ProductType.Frame to "Frame",
    ProductType.Glass to "Glass",
    ProductType.GeneralProduct to "General Product"
)

val productTypeLabelDisplayNames = mapOf(
    "Lens" to "Lens",
    "Frame" to "Frame",
    "Glass" to "Glass",
    "GeneralProduct" to "General Product"
)