package com.mkumar.viewmodel

import com.mkumar.data.CustomerFormState
import com.mkumar.data.db.entities.CustomerEntity

fun CustomerEntity.toUiModel(): CustomerFormState {
    return CustomerFormState(
        id = id,
        name = name,
        phone = phone
    )
}