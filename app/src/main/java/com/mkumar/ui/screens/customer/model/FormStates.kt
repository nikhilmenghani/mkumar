package com.mkumar.ui.screens.customer.model

import androidx.compose.runtime.Immutable

@Immutable
data class LensFormState(
    val description: String = "",
    val quantity: Int = 0,
    val unitPrice: Int = 0,
    val discountPercentage: Int = 0,
)

@Immutable
data class FrameFormState(
    val model: String = "",
    val quantity: Int = 0,
    val unitPrice: Int = 0,
    val discountPercentage: Int = 0,
)

@Immutable
data class ContactLensFormState(
    val brand: String = "",
    val quantity: Int = 0,
    val unitPrice: Int = 0,
    val discountPercentage: Int = 0,
)