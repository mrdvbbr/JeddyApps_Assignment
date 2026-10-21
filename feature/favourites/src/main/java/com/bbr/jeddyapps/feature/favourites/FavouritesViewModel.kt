package com.bbr.jeddyapps.feature.favourites

import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.bbr.jeddyapps.core.ui.viewmodel.BaseViewModel
import com.bbr.jeddyapps.domain.error.MovieError
import com.bbr.jeddyapps.domain.repository.MovieRepositoryException
import com.bbr.jeddyapps.domain.usecase.GetLikedMovies
import com.bbr.jeddyapps.domain.usecase.ToggleMovieLike
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@HiltViewModel
class FavouritesViewModel @Inject constructor(
    getLikedMovies: GetLikedMovies,
    private val toggleMovieLike: ToggleMovieLike,
) : BaseViewModel<FavouritesState, FavouritesIntent, FavouritesEffect>(FavouritesState) {

    val movies = getLikedMovies().cachedIn(viewModelScope)

    override fun onIntent(intent: FavouritesIntent) {
        when (intent) {
            is FavouritesIntent.ToggleLike -> viewModelScope.launch {
                try {
                    toggleMovieLike(intent.movie)
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: Exception) {
                    val error = (exception as? MovieRepositoryException)?.error ?: MovieError.UNKNOWN
                    mutableEffect.emit(FavouritesEffect.ShowError(error))
                }
            }
        }
    }
}
