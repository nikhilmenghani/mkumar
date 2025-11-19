package com.mkumar.data.db.enums

enum class OrderStatus {
    EMPTY,          // No items yet — ghost order
    ACTIVE,         // Has ≥1 items
    COMPLETED       // Optional future state
}
