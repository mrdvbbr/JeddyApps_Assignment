package com.bbr.jeddyapps.core.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [MovieEntity::class], version = 1, exportSchema = false)
abstract class MovieDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao

    companion object {
        private const val DATABASE_NAME = "jeddy_movies.db"

        fun create(context: Context): MovieDatabase =
            Room.databaseBuilder(context, MovieDatabase::class.java, DATABASE_NAME)
                .fallbackToDestructiveMigration(true)
                .build()
    }
}
