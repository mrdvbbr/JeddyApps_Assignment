package com.bbr.jeddyapps.feature.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.bbr.jeddyapps.core.ui.viewmodel.BaseViewModel
import com.bbr.jeddyapps.domain.error.MovieError
import com.bbr.jeddyapps.domain.repository.MovieRepositoryException
import com.bbr.jeddyapps.domain.usecase.GetMovieDetail
import com.bbr.jeddyapps.domain.usecase.ToggleMovieLike
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getMovieDetail: GetMovieDetail,
    private val toggleMovieLike: ToggleMovieLike,
) : BaseViewModel<DetailState, DetailIntent, DetailEffect>(DetailState()) {

    val movieId: String = checkNotNull(savedStateHandle["movieId"])

    init {
        loadDetail()
    }

    override fun onIntent(intent: DetailIntent) {
        when (intent) {
            DetailIntent.Retry -> loadDetail()
            DetailIntent.ToggleLike -> onToggleLike()
            DetailIntent.BackClicked -> onBackClicked()
        }
    }

    private fun loadDetail() {
        mutableState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val detail = getMovieDetail(movieId)
                mutableState.update { it.copy(isLoading = false, movie = detail, error = null) }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                val error = (exception as? MovieRepositoryException)?.error ?: MovieError.UNKNOWN
                mutableState.update { it.copy(isLoading = false, error = error) }
            }
        }
    }

    private fun onToggleLike() {
        val currentMovie = state.value.movie ?: return
        viewModelScope.launch {
            try {
                toggleMovieLike(currentMovie.toMovie())
                mutableState.update { state ->
                    state.copy(movie = currentMovie.copy(isLiked = !currentMovie.isLiked))
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                val error = (exception as? MovieRepositoryException)?.error ?: MovieError.UNKNOWN
                mutableEffect.emit(DetailEffect.ShowError(error))
            }
        }
    }

    private fun onBackClicked() {
        viewModelScope.launch {
            mutableEffect.emit(DetailEffect.NavigateBack)
        }
    }
}
