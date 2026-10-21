package com.bbr.jeddyapps.feature.list

import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.bbr.jeddyapps.core.ui.viewmodel.BaseViewModel
import com.bbr.jeddyapps.domain.model.Movie
import com.bbr.jeddyapps.domain.error.MovieError
import com.bbr.jeddyapps.domain.repository.MovieRepositoryException
import com.bbr.jeddyapps.domain.usecase.SearchMovies
import com.bbr.jeddyapps.domain.usecase.ToggleMovieLike
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val searchMovies: SearchMovies,
    private val toggleMovieLike: ToggleMovieLike,
) : BaseViewModel<HomeState, HomeIntent, HomeEffect>(HomeState()) {

    val movies = state.map { it.query.trim() }
        .debounce { if (it.isEmpty()) 0L else 350L }
        .distinctUntilChanged()
        .flatMapLatest { searchMovies(it.ifBlank { INITIAL_QUERY }) }
        .cachedIn(viewModelScope)

    override fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.QueryChanged -> onQueryChanged(intent.query)
            HomeIntent.ClearSearch -> onClearSearch()
            is HomeIntent.ToggleLike -> onToggleLike(intent.movie)
        }
    }

    private fun onQueryChanged(query: String) {
        mutableState.update { it.copy(query = query) }
    }

    private fun onClearSearch() {
        mutableState.update { it.copy(query = "") }
        viewModelScope.launch { mutableEffect.emit(HomeEffect.HideKeyboard) }
    }

    private fun onToggleLike(movie: Movie) {
        viewModelScope.launch {
            try {
                toggleMovieLike(movie)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                val error = (exception as? MovieRepositoryException)?.error ?: MovieError.UNKNOWN
                mutableEffect.emit(HomeEffect.ShowError(error))
            }
        }
    }

    private companion object {
        const val INITIAL_QUERY = "Batman"
    }
}
