package com.mkumar.repository.impl

import com.mkumar.common.extension.nowUtcMillis
import com.mkumar.data.db.dao.OutboxDao
import com.mkumar.data.db.entities.OutboxEntity
import com.mkumar.data.db.entities.OutboxEntity.Companion.STATUS_DONE
import com.mkumar.data.db.entities.OutboxEntity.Companion.STATUS_ERROR
import com.mkumar.data.db.entities.OutboxEntity.Companion.STATUS_QUEUED
import com.mkumar.repository.SyncRepository
import javax.inject.Inject

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

        val isUpsert = type.endsWith("_UPSERT", true)
        val isDelete = type.endsWith("_DELETE", true)

        // ---- DELETE overrides any pending UPSERT ----
        if (isDelete && entityId != null) {
            val upsertType = type.replace("_DELETE", "_UPSERT")
            outboxDao.deleteQueuedUpsertForEntity(upsertType, entityId)
        }

        // ---- UPSERT merge (dedup) ----
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

        // ---- Insert new entry ----
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

    override suspend fun cancelUpsertsFor(type: String, entityId: String) {
        outboxDao.deleteQueuedUpsertForEntity(type, entityId)
    }

    override suspend fun getPendingBatch(limit: Int): List<OutboxEntity> {
        return outboxDao.getByStatus(STATUS_QUEUED, limit)
    }

    override suspend fun markSuccess(id: String, newRemoteSha: String?) {
        val existing = outboxDao.getById(id) ?: return
        outboxDao.update(
            existing.copy(
                status = STATUS_DONE,
                updatedAt = nowUtcMillis(),
                lastKnownRemoteSha = newRemoteSha ?: existing.lastKnownRemoteSha,
                lastErrorMessage = null
            )
        )
    }

    override suspend fun markFailure(id: String, errorMessage: String) {
        val existing = outboxDao.getById(id) ?: return
        outboxDao.update(
            existing.copy(
                status = STATUS_ERROR,
                updatedAt = nowUtcMillis(),
                attemptCount = existing.attemptCount + 1,
                lastErrorMessage = errorMessage
            )
        )
    }

    override suspend fun clearAll() {
        outboxDao.clearAll()
    }
}
