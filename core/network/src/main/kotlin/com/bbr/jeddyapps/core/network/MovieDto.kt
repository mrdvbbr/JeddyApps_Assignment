package com.bbr.jeddyapps.core.network

import com.google.gson.annotations.SerializedName

data class MoviePageDto(
    @SerializedName("Search") val results: List<MovieDto>? = null,
    @SerializedName("totalResults") val totalResults: String? = null,
    @SerializedName("Response") val response: String? = null,
    @SerializedName("Error") val error: String? = null,
)

data class MovieDto(
    @SerializedName("imdbID") val id: String? = null,
    @SerializedName("Title") val title: String? = null,
    @SerializedName("Year") val year: String? = null,
    @SerializedName("Poster") val posterUrl: String? = null,
)
