package com.bbr.jeddyapps.feature.detail

import com.bbr.jeddyapps.domain.error.MovieError
import com.bbr.jeddyapps.domain.model.MovieDetail

data class DetailState(
    val isLoading: Boolean = true,
    val movie: MovieDetail? = null,
    val error: MovieError? = null,
)
