package com.mkumar.sync.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mkumar.common.extension.nowUtcMillis
import com.mkumar.data.db.dao.OutboxDao
import com.mkumar.data.db.entities.OutboxEntity
import com.mkumar.sync.remote.CloudRemote
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

@HiltWorker
class SyncOutboxWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val outboxDao: OutboxDao,
    private val cloud: CloudRemote,
    private val json: Json
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {

        // 1. Requeue stale IN_PROGRESS
        val cutoff = nowUtcMillis() - (5 * 60 * 1000) // 5 min
        outboxDao.requeueStaleInProgress(cutoff)

        // 2. Get QUEUED rows
        val ops = outboxDao.getByStatus(OutboxEntity.STATUS_QUEUED, 20)
        if (ops.isEmpty()) return@withContext Result.success()

        // 3. Process each operation
        for (op in ops) {
            try {
                outboxDao.markInProgress(op.id, nowUtcMillis())

                when (op.type) {

                    "CUSTOMER_UPSERT" ->
                        cloud.putJson(op.cloudPath!!, op.payloadJson)

                    "CUSTOMER_DELETE" ->
                        cloud.delete(op.cloudPath!!)

                    "ORDER_UPSERT" ->
                        cloud.putJson(op.cloudPath!!, op.payloadJson)

                    "ORDER_DELETE" ->
                        cloud.delete(op.cloudPath!!)

                    "PAYMENT_UPSERT" ->
                        cloud.putJson(op.cloudPath!!, op.payloadJson)

                    "PAYMENT_DELETE" ->
                        cloud.delete(op.cloudPath!!)

                    else ->
                        throw IllegalArgumentException("Unknown op type: ${op.type}")
                }

                outboxDao.markDone(op.id, nowUtcMillis())

            } catch (e: Exception) {
                outboxDao.markFailed(op.id, e.message ?: "Unknown error", nowUtcMillis())
                return@withContext Result.retry()
            }
        }

        Result.success()
    }
}

