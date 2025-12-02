package com.mkumar.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mkumar.common.extension.nowUtcMillis
import java.util.UUID

/**
 * Outbox entry for sync (write-path) operations to cloud (GitHub/GitLab).
 *
 * Each row is one logical operation, e.g. "CUSTOMER_UPSERT", with a self-contained JSON payload.
 * Sync workers will pick QUEUED rows, send them to cloud, and then mark them DONE / ERROR.
 */
@Entity(
    tableName = "outbox",
    indices = [
        Index(value = ["status"]),
        Index(value = ["createdAt"]),
        Index(value = ["entityId"])
    ]
)
data class OutboxEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    /** e.g. "CUSTOMER_UPSERT", "ORDER_UPSERT", "PAYMENT_UPSERT" */
    val type: String,

    /** JSON payload (DTO format) to be sent to cloud. */
    val payloadJson: String,

    /** "QUEUED" | "IN_PROGRESS" | "DONE" | "ERROR" */
    val status: String = STATUS_QUEUED,

    /** Number of failed attempts so far (for backoff / diagnostics). */
    val attemptCount: Int = 0,

    /** Last error message (if any) for debugging sync issues. */
    val lastErrorMessage: String? = null,

    /** Optional: target entity id (customerId, orderId, etc.) for debugging/optimizations. */
    val entityId: String? = null,

    /** Optional: precomputed cloud path (e.g. customers/<id>/profile.json). */
    val cloudPath: String? = null,

    /** Higher priority rows can be processed first if needed. */
    val priority: Int = 0,

    /** When this outbox entry was created (UTC millis). */
    val createdAt: Long = nowUtcMillis(),

    /** When this outbox entry was last updated (status change, retry, etc.). */
    val updatedAt: Long = nowUtcMillis(),

    /**
     * Logical "updatedAt" of the underlying entity at the time this op was generated.
     * Used for timestamp-based conflict resolution (latest wins).
     */
    val opUpdatedAt: Long = nowUtcMillis(),

    /** Last known remote sha (for GitHub/GitLab PUT-with-sha concurrency) if applicable. */
    val lastKnownRemoteSha: String? = null
) {
    companion object {
        const val STATUS_QUEUED = "QUEUED"
        const val STATUS_IN_PROGRESS = "IN_PROGRESS"
        const val STATUS_DONE = "DONE"
        const val STATUS_ERROR = "ERROR"
    }
}
