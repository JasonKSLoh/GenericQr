package com.lohjason.genericqr.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context

/**
 * HistoryDb
 * Created by jason on 27/6/18.
 */
@Database(entities = [(HistoryEntry::class)], version = 1, exportSchema = false)
abstract class HistoryDb : RoomDatabase() {

    abstract fun historyDao(): HistoryDao

    companion object {
        private var instance: HistoryDb? = null

        @Synchronized
        fun getInstance(context: Context): HistoryDb{
            if(instance == null){
                instance = Room.databaseBuilder(context.applicationContext,
                                                HistoryDb::class.java,
                                                "default")
                        .fallbackToDestructiveMigration()
                        .build()
            }
            return instance!!
        }
    }

}