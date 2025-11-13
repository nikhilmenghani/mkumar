package com.mkumar.data.services

import androidx.room.withTransaction
import com.mkumar.data.db.AppDatabase
import com.mkumar.data.db.entities.InvoiceCounterEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InvoiceNumberService @Inject constructor(
    private val db: AppDatabase
) {

    suspend fun takeNextInvoiceNumberInCurrentTx(): Long = db.withTransaction {
        val dao = db.invoiceCounterDao()

        val current = dao.getRow() ?: InvoiceCounterEntity(
            id = 1,
            lastNumber = 0L
        )

        val next = current.lastNumber + 1L
        dao.upsert(current.copy(lastNumber = next))

        next
    }
}
