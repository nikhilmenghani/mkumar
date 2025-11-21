package com.mkumar.viewmodel

import com.mkumar.data.db.entities.CustomerEntity
import com.mkumar.model.UiCustomerMini

fun CustomerEntity.toUiModel(): UiCustomerMini {
    return UiCustomerMini(
        id = id,
        name = name,
        phone = phone
    )
}