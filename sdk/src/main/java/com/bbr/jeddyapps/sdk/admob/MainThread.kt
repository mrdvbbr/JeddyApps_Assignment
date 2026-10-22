package com.bbr.jeddyapps.sdk.admob

import android.os.Handler
import android.os.Looper

private val mainHandler by lazy { Handler(Looper.getMainLooper()) }

internal fun onMainThread(action: () -> Unit) {
    if (Looper.myLooper() == Looper.getMainLooper()) action() else mainHandler.post(action)
}
