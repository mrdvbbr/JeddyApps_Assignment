package com.bbr.jeddyapps.core.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(movie: MovieEntity)

    @Query("DELETE FROM movies WHERE id = :id")
    suspend fun deleteById(id: String): Int

    @Query("SELECT EXISTS(SELECT 1 FROM movies WHERE id = :id)")
    fun isMovieLiked(id: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM movies WHERE id = :id)")
    suspend fun isMovieLikedSync(id: String): Boolean

    @Query("SELECT id FROM movies WHERE id IN (:ids)")
    suspend fun getLikedIds(ids: List<String>): List<String>

    @Query("SELECT id FROM movies WHERE id IN (:ids)")
    fun observeLikedIds(ids: List<String>): Flow<List<String>>

    @Query("SELECT * FROM movies WHERE id = :id")
    suspend fun getMovieById(id: String): MovieEntity?

    @Query("SELECT * FROM movies ORDER BY savedAt DESC")
    fun getAllMovies(): Flow<List<MovieEntity>>

    @Query("SELECT id FROM movies")
    fun getAllLikedMovieIds(): Flow<List<String>>

    @Query("SELECT * FROM movies ORDER BY savedAt DESC, id ASC")
    fun getLikedMoviesPagingSource(): PagingSource<Int, MovieEntity>
}
