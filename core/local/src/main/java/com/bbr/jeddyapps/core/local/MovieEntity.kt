package com.bbr.jeddyapps.core.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val year: String,
    val posterUrl: String?,
    val savedAt: Long = System.currentTimeMillis(),
)
