package com.mkumar.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mkumar.data.db.entities.OutboxEntity

@Dao
interface OutboxDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: OutboxEntity)

    @Update
    suspend fun update(entity: OutboxEntity)

    @Query("SELECT * FROM outbox WHERE id = :id")
    suspend fun getById(id: String): OutboxEntity?

    @Query("""
        SELECT * FROM outbox
        WHERE status = :status
        ORDER BY priority DESC, createdAt ASC
        LIMIT :limit
    """)
    suspend fun getByStatus(status: String, limit: Int = 50): List<OutboxEntity>

    @Query("UPDATE outbox SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, updatedAt: Long)

    @Query("DELETE FROM outbox WHERE status = :status")
    suspend fun deleteByStatus(status: String)

    @Query("DELETE FROM outbox")
    suspend fun clearAll()

    @Query("""
        SELECT * FROM outbox
        WHERE status = 'QUEUED'
        ORDER BY priority DESC, createdAt ASC
        LIMIT :limit
    """)
    suspend fun getQueuedOperations(limit: Int = 50): List<OutboxEntity>

    @Query("""
        UPDATE outbox
        SET status = 'IN_PROGRESS',
            updatedAt = :updatedAt,
            attemptCount = attemptCount + 1
        WHERE id = :id
    """)
    suspend fun markInProgress(id: String, updatedAt: Long)

    @Query("""
        UPDATE outbox
        SET status = 'DONE',
            updatedAt = :updatedAt,
            lastErrorMessage = NULL
        WHERE id = :id
    """)
    suspend fun markDone(id: String, updatedAt: Long)

    @Query("""
        UPDATE outbox
        SET status = 'ERROR',
            updatedAt = :updatedAt,
            lastErrorMessage = :error
        WHERE id = :id
    """)
    suspend fun markFailed(id: String, error: String?, updatedAt: Long)

    @Query("DELETE FROM outbox WHERE status = 'DONE'")
    suspend fun deleteCompleted()

    // ----- UPSERT DEDUP -----

    @Query("""
        SELECT * FROM outbox
        WHERE type = :type AND entityId IS :entityId
        AND status IN ('QUEUED','IN_PROGRESS')
        LIMIT 1
    """)
    suspend fun findQueuedOrInProgress(type: String, entityId: String?): OutboxEntity?

    @Query("""
        UPDATE outbox
        SET payloadJson = :payloadJson,
            cloudPath = :cloudPath,
            updatedAt = :updatedAt
        WHERE id = :id
    """)
    suspend fun updatePayload(
        id: String,
        payloadJson: String,
        cloudPath: String?,
        updatedAt: Long
    )

    // ----- DELETE overrides UPSERT -----

    @Query("""
        DELETE FROM outbox
        WHERE type = :upsertType
        AND entityId = :entityId
        AND status IN ('QUEUED','IN_PROGRESS')
    """)
    suspend fun deleteQueuedUpsertForEntity(
        upsertType: String,
        entityId: String
    )
}
