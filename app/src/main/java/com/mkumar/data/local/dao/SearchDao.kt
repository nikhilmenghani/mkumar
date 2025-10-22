// app/src/main/java/com/mkumar/data/local/dao/SearchDao.kt
package com.mkumar.data.local.dao

import androidx.room.Dao
import androidx.room.Query

@Dao
interface SearchDao {

    /**
     * Simple FTS query across name/phone.
     * Use '*' suffix for prefix search from UI layer as needed.
     */
    @Query("""
        SELECT rowid 
        FROM search_fts 
        WHERE search_fts MATCH :query 
        LIMIT :limit
    """)
    suspend fun searchIds(query: String, limit: Int = 50): List<Long>
}
