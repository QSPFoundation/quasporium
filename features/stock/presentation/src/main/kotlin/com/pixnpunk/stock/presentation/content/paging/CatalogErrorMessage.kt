package com.pixnpunk.stock.presentation.content.paging

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.pixnpunk.stock.domain.model.NetworkError
import com.pixnpunk.stock.domain.model.NetworkException
import com.pixnpunk.stock.presentation.R

@Composable
internal fun catalogErrorMessage(
    error: Throwable
): String =
    when (val networkError = (error as? NetworkException)?.error) {
        NetworkError.NoConnection -> stringResource(R.string.catalog_error_no_connection)
        NetworkError.Timeout -> stringResource(R.string.catalog_error_timeout)
        NetworkError.NotFound -> stringResource(R.string.catalog_error_not_found)
        is NetworkError.Server -> stringResource(R.string.catalog_error_server, networkError.code)
        NetworkError.Unexpected, null -> stringResource(R.string.catalog_error_unexpected)
    }
