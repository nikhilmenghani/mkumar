package com.mkumar.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mkumar.data.db.entities.OutboxEntity

@Dao
interface OutboxDao {

    // ----------------------------
    // INSERT / UPDATE
    // ----------------------------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: OutboxEntity)

    @Update
    suspend fun update(entity: OutboxEntity)

    // ----------------------------
    // FETCH
    // ----------------------------

    @Query("""
        SELECT * FROM outbox
        WHERE status = :status
        ORDER BY priority DESC, createdAt ASC
        LIMIT :limit
    """)
    suspend fun getByStatus(status: String, limit: Int): List<OutboxEntity>

    @Query("SELECT * FROM outbox WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): OutboxEntity?

    // ----------------------------
    // UPSERT MERGE
    // ----------------------------

    @Query("""
        SELECT * FROM outbox
        WHERE type = :type
        AND entityId = :entityId
        AND status IN ('QUEUED','IN_PROGRESS')
        LIMIT 1
    """)
    suspend fun findQueuedOrInProgress(type: String, entityId: String): OutboxEntity?

    // ----------------------------
    // DELETE UPSERT FOR DELETE OP
    // (NOTE: now cancels IN_PROGRESS too)
    // ----------------------------
    @Query("""
        DELETE FROM outbox
        WHERE type = :type
        AND entityId = :entityId
        AND status IN ('QUEUED','IN_PROGRESS')
    """)
    suspend fun deleteUpsertForDelete(type: String, entityId: String)

    // Old function preserved for compatibility:
    @Query("""
        DELETE FROM outbox
        WHERE type = :type
        AND entityId = :entityId
        AND status = 'QUEUED'
    """)
    suspend fun deleteQueuedUpsertForEntity(type: String, entityId: String)

    // ----------------------------
    // STALE IN_PROGRESS RECOVERY
    // ----------------------------

    @Query("""
        UPDATE outbox
        SET status = 'QUEUED'
        WHERE status = 'IN_PROGRESS'
        AND updatedAt < :cutoff
    """)
    suspend fun requeueStaleInProgress(cutoff: Long)

    // ----------------------------
    // STATUS MARKERS
    // ----------------------------
    @Query("UPDATE outbox SET status='IN_PROGRESS', updatedAt=:ts WHERE id=:id")
    suspend fun markInProgress(id: String, ts: Long)

    @Query("UPDATE outbox SET status='DONE', updatedAt=:ts WHERE id=:id")
    suspend fun markDone(id: String, ts: Long)

    @Query("""
        UPDATE outbox
        SET status='ERROR',
            updatedAt=:ts,
            attemptCount = attemptCount + 1,
            lastErrorMessage = :msg
        WHERE id = :id
    """)
    suspend fun markFailed(id: String, msg: String, ts: Long)

    @Query("DELETE FROM outbox")
    suspend fun clearAll()
}
