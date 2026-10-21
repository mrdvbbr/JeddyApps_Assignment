package com.bbr.jeddyapps.core.network

import retrofit2.http.GET
import retrofit2.http.Query

interface MovieApi {
    @GET("./")
    suspend fun searchMovies(
        @Query("s") query: String,
        @Query("page") page: Int,
        @Query("type") type: String = "movie",
    ): MoviePageDto

    @GET("./")
    suspend fun getMovieDetail(
        @Query("i") id: String,
        @Query("plot") plot: String = "full",
    ): MovieDetailDto
}
