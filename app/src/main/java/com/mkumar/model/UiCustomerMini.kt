package com.mkumar.model

data class UiCustomerMini(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String = "testName",
    val phone: String = "1234567890",
)
