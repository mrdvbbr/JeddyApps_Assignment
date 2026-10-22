package com.bbr.jeddyapps.sdk

import android.app.Activity
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import java.lang.ref.WeakReference
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Application-scoped ad orchestration. The host supplies a provider and its activity. */
class AdSdk(application: Application, val provider: AdProvider) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val processLifecycle = ProcessLifecycleOwner.get().lifecycle
    private val returnPolicy = SecondReturnPolicy(PreferencesVisitStore(application))
    private var activityReference = WeakReference<Activity>(null)
    private var started = false
    private var showOnThisReturn = false

    fun start() {
        if (started) return
        started = true
        processLifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                showOnThisReturn = returnPolicy.onForeground()
                if (returnPolicy.hasUpcomingOpportunity) preload()
            }

            override fun onResume(owner: LifecycleOwner) {
                activityReference.get()?.let(::showIfEligible)
            }

            override fun onStop(owner: LifecycleOwner) {
                showOnThisReturn = false
            }
        })
        preload()
    }

    /** Attach only the app's host activity, so an ad activity can never become the next host. */
    fun attach(activity: Activity) {
        require(activity is LifecycleOwner) { "The ad host must be a LifecycleOwner" }
        activityReference = WeakReference(activity)
        activity.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                showIfEligible(activity)
            }

            override fun onDestroy(owner: LifecycleOwner) {
                if (activityReference.get() === activity) activityReference.clear()
                owner.lifecycle.removeObserver(this)
            }
        })
    }

    private fun preload() {
        scope.launch {
            try {
                provider.initialize()
                if (returnPolicy.hasUpcomingOpportunity) provider.preloadInterstitial()
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Log.w("AdSdk", "Ads could not be initialized", error)
            }
        }
    }

    private fun showIfEligible(activity: Activity) {
        val lifecycle = (activity as LifecycleOwner).lifecycle
        if (!showOnThisReturn || activity.isFinishing || activity.isDestroyed ||
            !processLifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) ||
            !lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
        ) return

        // Consume before showing: dismissal, rotation and slow loads must never trigger another ad.
        showOnThisReturn = false
        try {
            provider.showInterstitial(activity)
        } catch (error: Exception) {
            Log.w("AdSdk", "Interstitial could not be shown", error)
        }
    }
}

private class PreferencesVisitStore(context: Context) : ForegroundVisitStore {
    private val preferences = context.getSharedPreferences("ad_sdk_returns", Context.MODE_PRIVATE)

    override var visits: Int
        get() = preferences.getInt("foreground_visits", 0)
        set(value) { preferences.edit().putInt("foreground_visits", value).apply() }
}
