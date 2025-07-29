package com.mkumar.data.validation

data class ValidationResult(
    val isValid: Boolean,
    val errors: Map<String, String> = emptyMap()
)