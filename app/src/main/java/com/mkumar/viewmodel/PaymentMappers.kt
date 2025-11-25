package com.mkumar.viewmodel

import com.mkumar.data.db.entities.PaymentEntity
import com.mkumar.model.UiPaymentItem

fun PaymentEntity.toUiPaymentItem(): UiPaymentItem {
    return UiPaymentItem(
        id = id,
        orderId = orderId,
        amountPaid = amountPaid,
        paymentAt = paymentAt
    )
}