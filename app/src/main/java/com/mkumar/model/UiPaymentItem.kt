package com.mkumar.model

data class UiPaymentItem(
    val id: String,
    val orderId: String,
    val amountPaid: Int,
    val paymentAt: Long
)