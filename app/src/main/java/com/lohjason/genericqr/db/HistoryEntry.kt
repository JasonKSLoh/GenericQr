package com.lohjason.genericqr.db

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

/**
 * HistoryEntry
 * Created by jason on 26/6/18.
 */
@Entity
data class HistoryEntry(@PrimaryKey
                        @ColumnInfo(name = "id")
                        var id: Int? = null,
                        @ColumnInfo(name = "data")
                        var data: String,
                        @ColumnInfo(name = "type")
                        var type: String,
                        @ColumnInfo(name = "timestamp")
                        var timestamp: Long)