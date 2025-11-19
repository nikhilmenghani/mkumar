package com.mkumar.model

/**
 * Stored as STRING in DB for readability & future-proofing.
 * Reordering enum values will NOT break the database.
 */
enum class OrderStatus(val value: String) {
    DRAFT("DRAFT"),
    ACTIVE("ACTIVE"),
    COMPLETED("COMPLETED");

    companion object {
        fun from(value: String?): OrderStatus =
            entries.firstOrNull { it.value == value } ?: DRAFT
    }
}
