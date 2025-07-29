package com.mkumar.data

sealed class ProductFormData {
    data class FrameData(
        val brand: String = "",
        val color: String = "",
        val size: String = ""
    ) : ProductFormData()

    data class LensData(
        val leftSphere: String = "",
        val leftAxis: String = "",
        val rightSphere: String = "",
        val rightAxis: String = ""
    ) : ProductFormData()

    data class ContactLensData(
        val power: String = "",
        val duration: String = ""
    ) : ProductFormData()
}
