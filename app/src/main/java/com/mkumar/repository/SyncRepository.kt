package com.mkumar.repository

import com.mkumar.data.db.entities.OutboxEntity

/**
 * Repository facade for enqueueing sync operations and for workers to manage the outbox.
 *
 * UI / domain code should call enqueue* methods when writing to local DB.
 * Workers should call getPendingBatch / markSuccess / markFailure.
 */
interface SyncRepository {

    /**
     * Enqueue a new operation in the outbox.
     *
     * @return the generated outbox id (primary key).
     */
    suspend fun enqueueOperation(
        type: String,
        payloadJson: String,
        entityId: String? = null,
        cloudPath: String? = null,
        priority: Int = 0,
        opUpdatedAt: Long? = null,
        lastKnownRemoteSha: String? = null
    ): String

    /**
     * Fetch a batch of queued operations for processing.
     */
    suspend fun getPendingBatch(
        limit: Int = 50
    ): List<OutboxEntity>

    /**
     * Mark operation as successfully synced.
     */
    suspend fun markSuccess(
        id: String,
        newRemoteSha: String? = null
    )

    /**
     * Mark operation as failed; caller (worker) decides whether to retry later or not.
     */
    suspend fun markFailure(
        id: String,
        errorMessage: String
    )

    /**
     * Optional utility to clear all outbox entries (e.g. debug/reset).
     */
    suspend fun clearAll()
}
