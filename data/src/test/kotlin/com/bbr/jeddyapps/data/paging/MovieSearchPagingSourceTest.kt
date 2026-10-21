package com.bbr.jeddyapps.data.paging

import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.bbr.jeddyapps.domain.model.Movie
import com.bbr.jeddyapps.domain.model.MoviePage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class MovieSearchPagingSourceTest {
    @Test
    fun `refresh loads the first API page even with a larger requested load size`() = runTest {
        val source = source { page ->
            assertEquals(1, page)
            MoviePage(listOf(movie), page, 3)
        }

        val result = source.load(PagingSource.LoadParams.Refresh(null, 30, false))
            as PagingSource.LoadResult.Page<Int, Movie>

        assertEquals(listOf(movie), result.data)
        assertNull(result.prevKey)
        assertEquals(2, result.nextKey)
    }

    @Test
    fun `last page has a previous key and no next key`() = runTest {
        val result = source { page -> MoviePage(listOf(movie), page, 3) }
            .load(PagingSource.LoadParams.Append(3, 10, false)) as PagingSource.LoadResult.Page<Int, Movie>

        assertEquals(2, result.prevKey)
        assertNull(result.nextKey)
    }

    @Test
    fun `empty page ends pagination`() = runTest {
        val result = source { page -> MoviePage(emptyList(), page, 3) }
            .load(PagingSource.LoadParams.Append(2, 10, false)) as PagingSource.LoadResult.Page<Int, Movie>

        assertEquals(emptyList<Movie>(), result.data)
        assertNull(result.nextKey)
    }

    @Test
    fun `load errors are exposed for Paging retry`() = runTest {
        val failure = Exception("Offline")
        val result = source { _ -> throw failure }
            .load(PagingSource.LoadParams.Refresh(null, 10, false)) as PagingSource.LoadResult.Error<Int, Movie>

        assertSame(failure, result.throwable)
    }

    @Test
    fun `cancellation propagates instead of becoming a load error`() = runTest {
        val cancellation = CancellationException()
        try {
            source { _ -> throw cancellation }.load(PagingSource.LoadParams.Refresh(null, 10, false))
            throw AssertionError("Expected cancellation")
        } catch (exception: CancellationException) {
            assertSame(cancellation, exception)
        }
    }

    @Test
    fun `refresh key uses the page nearest the visible item`() {
        val state = PagingState(
            pages = listOf(
                PagingSource.LoadResult.Page(List(10) { movie }, prevKey = null, nextKey = 2),
                PagingSource.LoadResult.Page(List(10) { movie }, prevKey = 1, nextKey = 3),
            ),
            anchorPosition = 12,
            config = PagingConfig(pageSize = 10),
            leadingPlaceholderCount = 0,
        )

        assertEquals(2, source { page -> MoviePage(emptyList(), page, 0) }.getRefreshKey(state))
    }

    private fun source(loadPage: suspend (Int) -> MoviePage) = MovieSearchPagingSource(loadPage)

    private companion object {
        val movie = Movie("tt0078748", "Alien", "1979", null)
    }
}
