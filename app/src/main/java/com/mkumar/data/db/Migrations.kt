package com.mkumar.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE orders ADD COLUMN invoice_seq INTEGER"
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS invoice_counter (
                id INTEGER NOT NULL PRIMARY KEY,
                lastNumber INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            "INSERT INTO invoice_counter (id, lastNumber) VALUES (1, 0)"
        )
    }
}
