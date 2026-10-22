package com.bbr.jeddyapps.sdk

internal interface ForegroundVisitStore {
    var visits: Int
}

/** Initial foreground is visit 1; the second return is visit 3, once per installation. */
internal class SecondReturnPolicy(private val store: ForegroundVisitStore) {
    val hasUpcomingOpportunity: Boolean
        get() = store.visits < SECOND_RETURN_VISIT

    fun onForeground(): Boolean {
        val previous = store.visits.coerceIn(0, SECOND_RETURN_VISIT)
        if (previous < SECOND_RETURN_VISIT) store.visits = previous + 1
        return previous == SECOND_RETURN_VISIT - 1
    }

    private companion object {
        const val SECOND_RETURN_VISIT = 3
    }
}
