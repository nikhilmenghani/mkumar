package com.mkumar.repository.impl

import com.mkumar.common.extension.nowUtcMillis
import com.mkumar.data.db.dao.OutboxDao
import com.mkumar.data.db.entities.OutboxEntity
import com.mkumar.data.db.entities.OutboxEntity.Companion.STATUS_DONE
import com.mkumar.data.db.entities.OutboxEntity.Companion.STATUS_ERROR
import com.mkumar.data.db.entities.OutboxEntity.Companion.STATUS_QUEUED
import com.mkumar.repository.SyncRepository
import javax.inject.Inject

/**
 * SyncRepository with de-duplication:
 *
 * - UPSERT ops merge if same (type + entityId) exists in QUEUED or IN_PROGRESS.
 * - DELETE ops always insert new (never merged).
 * - Ensures most recent payload is persisted.
 */
class SyncRepositoryImpl @Inject constructor(
    private val outboxDao: OutboxDao
) : SyncRepository {

    override suspend fun enqueueOperation(
        type: String,
        payloadJson: String,
        entityId: String?,
        cloudPath: String?,
        priority: Int,
        opUpdatedAt: Long?,
        lastKnownRemoteSha: String?
    ): String {

        val now = opUpdatedAt ?: nowUtcMillis()

        val isUpsert = type.endsWith("_UPSERT", ignoreCase = true)
        val isDelete = type.endsWith("_DELETE", ignoreCase = true)

        // ------------------------------------------------------------
        // 1. For UPSERT ops: try merging with existing QUEUED/IN_PROGRESS entry
        // ------------------------------------------------------------
        if (isUpsert && entityId != null) {
            val existing = outboxDao.findQueuedOrInProgress(type, entityId)
            if (existing != null) {
                outboxDao.updatePayload(
                    id = existing.id,
                    payloadJson = payloadJson,
                    cloudPath = cloudPath,
                    updatedAt = now
                )
                return existing.id
            }
        }

        // ------------------------------------------------------------
        // 2. For DELETE ops: ALWAYS insert (override any pending upsert)
        // ------------------------------------------------------------

        // ------------------------------------------------------------
        // 3. Insert new entry
        // ------------------------------------------------------------
        val entity = OutboxEntity(
            type = type,
            payloadJson = payloadJson,
            status = STATUS_QUEUED,
            attemptCount = 0,
            lastErrorMessage = null,
            entityId = entityId,
            cloudPath = cloudPath,
            priority = priority,
            createdAt = now,
            updatedAt = now,
            opUpdatedAt = now,
            lastKnownRemoteSha = lastKnownRemoteSha
        )

        outboxDao.insert(entity)
        return entity.id
    }

    override suspend fun getPendingBatch(limit: Int): List<OutboxEntity> {
        return outboxDao.getByStatus(STATUS_QUEUED, limit)
    }

    override suspend fun markSuccess(id: String, newRemoteSha: String?) {
        val existing = outboxDao.getById(id) ?: return
        val updated = existing.copy(
            status = STATUS_DONE,
            updatedAt = nowUtcMillis(),
            lastErrorMessage = null,
            lastKnownRemoteSha = newRemoteSha ?: existing.lastKnownRemoteSha
        )
        outboxDao.update(updated)
    }

    override suspend fun markFailure(id: String, errorMessage: String) {
        val existing = outboxDao.getById(id) ?: return
        val updated = existing.copy(
            status = STATUS_ERROR,
            updatedAt = nowUtcMillis(),
            attemptCount = existing.attemptCount + 1,
            lastErrorMessage = errorMessage
        )
        outboxDao.update(updated)
    }

    override suspend fun clearAll() {
        outboxDao.clearAll()
    }
}
