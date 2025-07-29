package com.mkumar.data

sealed class ProductType(val label: String) {
    object Frame : ProductType("Frame")
    object Lens : ProductType("Lens")
    object ContactLens : ProductType("Contact Lens")

    companion object {
        val allTypes = listOf(Frame, Lens, ContactLens)
    }
}
