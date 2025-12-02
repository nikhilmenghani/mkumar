package com.mkumar.sync.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mkumar.common.extension.nowUtcMillis
import com.mkumar.data.db.dao.OutboxDao
import com.mkumar.data.db.entities.OutboxEntity
import com.mkumar.sync.remote.CloudRemote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject

class SyncOutboxWorker @Inject constructor(
    appContext: Context,
    params: WorkerParameters,
    private val outboxDao: OutboxDao,
    private val cloud: CloudRemote,
    private val json: Json
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val ops = outboxDao.getQueuedOperations(limit = 20)
        if (ops.isEmpty()) return@withContext Result.success()

        ops.forEach { op ->
            try {
                outboxDao.markInProgress(op.id, nowUtcMillis())
                processOperation(op)
                outboxDao.markDone(op.id, nowUtcMillis())
            } catch (e: Exception) {
                outboxDao.markFailed(op.id, e.message ?: "Unknown error", nowUtcMillis())
                return@withContext Result.retry()
            }
        }

        Result.success()
    }

    private suspend fun processOperation(op: OutboxEntity) {
        when (op.type) {

            // -----------------------------------------
            // CUSTOMER
            // -----------------------------------------
            "CUSTOMER_UPSERT" -> {
                cloud.putJson(path = op.cloudPath!!, content = op.payloadJson)
            }

            // -----------------------------------------
            // ORDER
            // -----------------------------------------

            "ORDER_DELETE" -> {
                cloud.delete(op.cloudPath!!)
                outboxDao.markDone(op.id, nowUtcMillis())
            }

            "ORDER_UPSERT" -> {
                val content = op.payloadJson
                cloud.putJson(op.cloudPath!!, content)
                outboxDao.markDone(op.id, nowUtcMillis())
            }

            // -----------------------------------------
            // PAYMENT
            // -----------------------------------------
            "PAYMENT_UPSERT" -> {
                // Write/update payment JSON
                cloud.putJson(path = op.cloudPath!!, content = op.payloadJson)
            }

            "PAYMENT_DELETE" -> {
                // Delete remote file OR mark as tombstone file
                cloud.delete(path = op.cloudPath!!)
            }

            else -> {
                throw IllegalArgumentException("Unknown Outbox op type: ${op.type}")
            }
        }
    }
}
