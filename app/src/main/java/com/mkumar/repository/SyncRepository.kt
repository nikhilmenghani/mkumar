package com.mkumar.repository

import com.mkumar.data.db.entities.OutboxEntity

interface SyncRepository {

    suspend fun enqueueOperation(
        type: String,
        payloadJson: String,
        entityId: String?,
        cloudPath: String?,
        priority: Int = 0,
        opUpdatedAt: Long? = null,
        lastKnownRemoteSha: String? = null
    ): String

    suspend fun getPendingBatch(limit: Int): List<OutboxEntity>

    suspend fun markSuccess(id: String, newRemoteSha: String? = null)

    suspend fun markFailure(id: String, errorMessage: String)

    suspend fun clearAll()

    suspend fun cancelUpsertsFor(type: String, entityId: String)
}
