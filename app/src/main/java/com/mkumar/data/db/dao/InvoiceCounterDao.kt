package com.mkumar.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mkumar.data.db.entities.InvoiceCounterEntity

@Dao
interface InvoiceCounterDao {

    @Query("SELECT * FROM invoice_counter WHERE id = 1")
    suspend fun getRow(): InvoiceCounterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(row: InvoiceCounterEntity)
}
