package com.mkumar.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mkumar.common.extension.nowUtcMillis
import java.util.UUID

/**
 * Outbox for sync (write-path) operations to GitHub.
 * payloadJson contains the operation body; keep it small and self-contained.
 */
@Entity(
    tableName = "outbox",
    indices = [
        Index(value = ["status"]),
        Index(value = ["createdAt"])
    ]
)
data class OutboxEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    /** e.g., "UpsertProfile", "CreateOrder" */
    val type: String,

    /** JSON payload to be sent (DTO shape) */
    val payloadJson: String,

    /** "QUEUED" | "IN_PROGRESS" | "DONE" | "ERROR" */
    val status: String = "QUEUED",

    val attemptCount: Int = 0,

    val lastErrorMessage: String? = null,

    val createdAt: Long = nowUtcMillis(),
    val updatedAt: Long = nowUtcMillis(),

    /** For sha-based concurrency on profiles/files if needed later */
    val lastKnownRemoteSha: String? = null
)