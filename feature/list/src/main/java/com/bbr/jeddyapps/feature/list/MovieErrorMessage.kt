package com.bbr.jeddyapps.feature.list

import androidx.annotation.StringRes
import com.bbr.jeddyapps.core.ui.R as CoreUiR
import com.bbr.jeddyapps.domain.error.MovieError
import com.bbr.jeddyapps.domain.repository.MovieRepositoryException

@StringRes
internal fun Throwable.messageResource(): Int = when ((this as? MovieRepositoryException)?.error) {
    MovieError.NETWORK -> CoreUiR.string.error_network
    MovieError.SERVICE_UNAVAILABLE -> CoreUiR.string.error_service_unavailable
    MovieError.RATE_LIMITED -> CoreUiR.string.error_rate_limited
    MovieError.INVALID_QUERY -> R.string.error_invalid_query
    MovieError.INVALID_RESPONSE -> CoreUiR.string.error_invalid_response
    MovieError.UNKNOWN, null -> CoreUiR.string.error_unknown
}
