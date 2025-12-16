package com.mkumar.repository.impl

import android.content.Context
import com.mkumar.common.extension.nowUtcMillis
import com.mkumar.data.db.dao.OutboxDao
import com.mkumar.data.db.entities.OutboxEntity
import com.mkumar.repository.SyncRepository
import com.mkumar.sync.SyncScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SyncRepositoryImpl @Inject constructor(
    private val outboxDao: OutboxDao,
    @ApplicationContext private val context: Context
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

        // -----------------------------
        // DELETE overrides UPSERT (queued or in-progress)
        // -----------------------------
        if (isDelete && entityId != null) {
            val upsertType = type.replace("_DELETE", "_UPSERT")
            outboxDao.deleteUpsertForDelete(upsertType, entityId)
        }

        // -----------------------------
        // UPSERT MERGE (dedupe)
        // -----------------------------
        if (isUpsert && entityId != null) {
            val existing = outboxDao.findQueuedOrInProgress(type, entityId)
            if (existing != null) {
                val updated = existing.copy(
                    payloadJson = payloadJson,
                    cloudPath = cloudPath,
                    updatedAt = now,
                    opUpdatedAt = now
                )
                outboxDao.update(updated)
                return existing.id
            }
        }

        // -----------------------------
        // INSERT NEW ENTRY
        // -----------------------------
        val entity = OutboxEntity(
            type = type,
            payloadJson = payloadJson,
            status = OutboxEntity.STATUS_QUEUED,
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
        SyncScheduler.enqueuePushSync(context)
        return entity.id
    }

    override suspend fun cancelUpsertsFor(type: String, entityId: String) {
        outboxDao.deleteUpsertForDelete(type, entityId)
    }

    override suspend fun getPendingBatch(limit: Int): List<OutboxEntity> {
        return outboxDao.getByStatus(OutboxEntity.STATUS_QUEUED, limit)
    }

    override suspend fun markSuccess(id: String, newRemoteSha: String?) {
        val e = outboxDao.getById(id) ?: return
        outboxDao.update(
            e.copy(
                status = OutboxEntity.STATUS_DONE,
                updatedAt = nowUtcMillis(),
                lastKnownRemoteSha = newRemoteSha ?: e.lastKnownRemoteSha,
                lastErrorMessage = null
            )
        )
    }

    override suspend fun markFailure(id: String, errorMessage: String) {
        val e = outboxDao.getById(id) ?: return
        outboxDao.update(
            e.copy(
                status = OutboxEntity.STATUS_ERROR,
                updatedAt = nowUtcMillis(),
                attemptCount = e.attemptCount + 1,
                lastErrorMessage = errorMessage
            )
        )
    }

    override suspend fun clearAll() {
        outboxDao.clearAll()
    }
}

