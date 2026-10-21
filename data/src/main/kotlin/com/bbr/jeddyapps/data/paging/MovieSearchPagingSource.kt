package com.bbr.jeddyapps.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.bbr.jeddyapps.domain.model.Movie
import com.bbr.jeddyapps.domain.model.MoviePage
import kotlinx.coroutines.CancellationException

internal class MovieSearchPagingSource(
    private val loadPage: suspend (Int) -> MoviePage,
) : PagingSource<Int, Movie>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> {
        val page = params.key ?: 1
        return try {
            val result = loadPage(page)
            LoadResult.Page(
                data = result.movies,
                prevKey = if (page > 1) page - 1 else null,
                nextKey = if (result.movies.isNotEmpty() && page < result.totalPages) page + 1 else null,
            )
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Movie>): Int? {
        val anchor = state.anchorPosition ?: return null
        val page = state.closestPageToPosition(anchor) ?: return null
        return page.prevKey?.plus(1) ?: page.nextKey?.minus(1)
    }
}
