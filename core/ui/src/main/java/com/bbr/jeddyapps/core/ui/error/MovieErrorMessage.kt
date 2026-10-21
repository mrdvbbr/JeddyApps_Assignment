package com.bbr.jeddyapps.core.ui.error

import androidx.annotation.StringRes
import com.bbr.jeddyapps.core.ui.R as CoreUiR
import com.bbr.jeddyapps.domain.error.MovieError
import com.bbr.jeddyapps.domain.repository.MovieRepositoryException

@StringRes
fun Throwable.messageResource(): Int = ((this as? MovieRepositoryException)?.error ?: MovieError.UNKNOWN)
    .messageResource()

@StringRes
fun MovieError.messageResource(): Int = when (this) {
    MovieError.NETWORK -> CoreUiR.string.error_network
    MovieError.SERVICE_UNAVAILABLE -> CoreUiR.string.error_service_unavailable
    MovieError.RATE_LIMITED -> CoreUiR.string.error_rate_limited
    MovieError.INVALID_QUERY -> CoreUiR.string.error_invalid_query
    MovieError.INVALID_RESPONSE -> CoreUiR.string.error_invalid_response
    MovieError.UNKNOWN -> CoreUiR.string.error_unknown
}
