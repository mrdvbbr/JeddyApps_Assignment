package com.bbr.jeddyapps.feature.list

import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.bbr.jeddyapps.core.ui.viewmodel.BaseViewModel
import com.bbr.jeddyapps.domain.usecase.SearchMovies
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
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
) : BaseViewModel<HomeState, HomeIntent, HomeEffect>(HomeState()) {

    val movies = state.map { it.query.trim() }
        .debounce { if (it.isEmpty()) 0L else 350L }
        .distinctUntilChanged()
        .flatMapLatest { searchMovies(it.ifBlank { INITIAL_QUERY }) }
        .cachedIn(viewModelScope)

    override fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.QueryChanged -> mutableState.update { it.copy(query = intent.query) }
            HomeIntent.ClearSearch -> {
                mutableState.update { it.copy(query = "") }
                viewModelScope.launch { mutableEffect.emit(HomeEffect.HideKeyboard) }
            }
        }
    }

    private companion object {
        const val INITIAL_QUERY = "Batman"
    }
}
