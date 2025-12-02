package com.mkumar.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mkumar.data.db.entities.OutboxEntity

@Dao
interface OutboxDao {

    // ---------------------------------------------------------
    // BASIC CRUD
    // ---------------------------------------------------------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: OutboxEntity)

    @Update
    suspend fun update(entity: OutboxEntity)

    @Query("SELECT * FROM outbox WHERE id = :id")
    suspend fun getById(id: String): OutboxEntity?

    /**
     * Get a batch of outbox entries by status, ordered by priority (desc) and createdAt (asc).
     * Used by higher-level code that wants explicit status-based fetching.
     */
    @Query(
        """
        SELECT * FROM outbox
        WHERE status = :status
        ORDER BY priority DESC, createdAt ASC
        LIMIT :limit
        """
    )
    suspend fun getByStatus(
        status: String,
        limit: Int = 50
    ): List<OutboxEntity>

    /**
     * Mark a specific entry's status without fetching it first.
     */
    @Query("UPDATE outbox SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(
        id: String,
        status: String,
        updatedAt: Long
    )

    @Query("DELETE FROM outbox WHERE status = :status")
    suspend fun deleteByStatus(status: String)

    @Query("DELETE FROM outbox")
    suspend fun clearAll()

    // ---------------------------------------------------------
    // WORKER-ORIENTED HELPERS
    // ---------------------------------------------------------

    /**
     * Fetch next queued operations for the worker.
     * This is basically a convenience wrapper over getByStatus("QUEUED").
     */
    @Query(
        """
        SELECT * FROM outbox
        WHERE status = 'QUEUED'
        ORDER BY priority DESC, createdAt ASC
        LIMIT :limit
        """
    )
    suspend fun getQueuedOperations(limit: Int = 50): List<OutboxEntity>

    /**
     * Mark an entry as IN_PROGRESS and increment attemptCount.
     */
    @Query(
        """
        UPDATE outbox
        SET status = 'IN_PROGRESS',
            updatedAt = :updatedAt,
            attemptCount = attemptCount + 1
        WHERE id = :id
        """
    )
    suspend fun markInProgress(
        id: String,
        updatedAt: Long = System.currentTimeMillis()
    )

    /**
     * Mark an entry as DONE and clear lastErrorMessage.
     */
    @Query(
        """
        UPDATE outbox
        SET status = 'DONE',
            updatedAt = :updatedAt,
            lastErrorMessage = NULL
        WHERE id = :id
        """
    )
    suspend fun markDone(
        id: String,
        updatedAt: Long = System.currentTimeMillis()
    )

    /**
     * Mark an entry as ERROR and store lastErrorMessage.
     */
    @Query(
        """
        UPDATE outbox
        SET status = 'ERROR',
            updatedAt = :updatedAt,
            lastErrorMessage = :error
        WHERE id = :id
        """
    )
    suspend fun markFailed(
        id: String,
        error: String?,
        updatedAt: Long = System.currentTimeMillis()
    )

    /**
     * Optional: prune all completed entries.
     */
    @Query("DELETE FROM outbox WHERE status = 'DONE'")
    suspend fun deleteCompleted()

    @Query("""
    SELECT * FROM outbox
    WHERE type = :type AND entityId IS :entityId
    AND status IN ('QUEUED', 'IN_PROGRESS')
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

}
