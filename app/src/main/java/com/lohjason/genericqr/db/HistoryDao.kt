package com.lohjason.genericqr.db

import android.arch.persistence.room.*
import io.reactivex.Single

/**
 * HistoryDao
 * Created by jason on 27/6/18.
 */
@Dao
abstract class HistoryDao {

    @Query("SELECT * FROM historyentry")
    abstract fun getAllEntries(): Single<List<HistoryEntry>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insertItem(historyEntry: HistoryEntry): Long

    @Update(onConflict = OnConflictStrategy.IGNORE)
    abstract fun updateItem(historyEntry: HistoryEntry): Int

    @Transaction
    open fun insertOrUpdateItem(historyEntry: HistoryEntry): Long {
        val numRowsModified = insertItem(historyEntry)
        return if (numRowsModified <= 0) {
            updateItem(historyEntry).toLong()
        } else {
            numRowsModified
        }
    }

    @Delete
    abstract fun deleteItem(historyEntry: HistoryEntry): Int


    @Query("DELETE FROM historyentry WHERE id IN (:ids)")
    abstract fun deleteItemsById(ids: List<Int>): Int

    @Query("DELETE FROM historyentry")
    abstract fun deleteAllItems(): Int
}