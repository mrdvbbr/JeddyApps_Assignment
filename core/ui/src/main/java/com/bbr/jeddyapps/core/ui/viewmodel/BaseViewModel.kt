package com.bbr.jeddyapps.core.ui.viewmodel

import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class BaseViewModel<UIState, Intent, SideEffect>(initialState: UIState) : ViewModel() {
    protected val mutableState = MutableStateFlow<UIState>(initialState)
    protected val mutableEffect = MutableSharedFlow<SideEffect>()

    val state: StateFlow<UIState> = mutableState.asStateFlow()
    val sideEffects: SharedFlow<SideEffect> = mutableEffect.asSharedFlow()
    abstract fun onIntent(intent: Intent)
}
