package com.bbr.jeddyapps.data

import androidx.paging.testing.ErrorRecovery
import androidx.paging.testing.asSnapshot
import com.bbr.jeddyapps.core.network.MovieApi
import com.bbr.jeddyapps.core.network.MovieDto
import com.bbr.jeddyapps.core.network.MoviePageDto
import com.bbr.jeddyapps.domain.repository.MovieRepositoryException
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.io.IOException
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class OmdbMovieRepositoryTest {
    private val dataSource = FakeMovieNetworkDataSource()
    private val repository = OmdbMovieRepository(dataSource)

    @Test
    fun `paged flow maps OMDb JSON into movie metadata`() = runTest(timeout = 5.seconds) {
        dataSource.response = Gson().fromJson(
            """{
                "Search":[{"imdbID":"tt1234567","Title":"A Movie","Year":"2020–2021","Poster":"https://example.com/poster.jpg"}],
                "totalResults":"1","Response":"True"
            }""".trimIndent(),
            MoviePageDto::class.java,
        )

        val movie = repository.searchMovies("A Movie").asSnapshot().single()

        assertEquals("tt1234567", movie.id)
        assertEquals("A Movie", movie.title)
        assertEquals("2020–2021", movie.year)
        assertEquals("https://example.com/poster.jpg", movie.posterUrl)
    }

    @Test
    fun `repository constructs paging without loading until collected`() = runTest(timeout = 5.seconds) {
        val movies = repository.searchMovies("A Movie")
        assertFalse(dataSource.called)

        movies.asSnapshot()

        assertEquals(listOf("A Movie" to 1), dataSource.requests)
    }

    @Test
    fun `initial load requests only the first ten movies`() = runTest(timeout = 5.seconds) {
        dataSource.handler = { _, page -> pageResponse(page, total = 25) }

        val movies = repository.searchMovies("A Movie").asSnapshot()

        assertEquals(10, movies.size)
        assertEquals(listOf("A Movie" to 1), dataSource.requests)
    }

    @Test
    fun `scrolling loads subsequent API pages with the trimmed query`() = runTest(timeout = 5.seconds) {
        dataSource.handler = { _, page -> pageResponse(page, total = 25) }

        val movies = repository.searchMovies("  A Movie  ").asSnapshot { scrollTo(24) }

        assertEquals(25, movies.size)
        assertEquals(25, movies.distinctBy { it.id }.size)
        assertEquals(listOf("A Movie" to 1, "A Movie" to 2, "A Movie" to 3), dataSource.requests)
    }

    @Test
    fun `failed append retries without dropping previous pages`() = runTest(timeout = 5.seconds) {
        var failed = false
        dataSource.handler = { _, page ->
            if (page == 2 && !failed) {
                failed = true
                throw IOException("Offline")
            }
            pageResponse(page, total = 20)
        }

        val movies = repository.searchMovies("A Movie").asSnapshot(onError = { ErrorRecovery.RETRY }) {
            scrollTo(19)
        }

        assertEquals(20, movies.size)
        assertEquals(listOf("A Movie" to 1, "A Movie" to 2, "A Movie" to 2), dataSource.requests)
    }

    @Test
    fun `each collection creates a fresh paging source`() = runTest(timeout = 5.seconds) {
        val movies = repository.searchMovies("A Movie")

        movies.asSnapshot()
        movies.asSnapshot()

        assertEquals(listOf("A Movie" to 1, "A Movie" to 1), dataSource.requests)
    }

    @Test
    fun `movie not found is an empty page rather than an error`() = runTest(timeout = 5.seconds) {
        dataSource.response = failure("Movie not found!")

        assertTrue(repository.searchMovies("Unknown title").asSnapshot().isEmpty())
        assertEquals(listOf("Unknown title" to 1), dataSource.requests)
    }

    @Test
    fun `API key rejection is exposed through paging error state`() = runTest(timeout = 5.seconds) {
        dataSource.response = failure("Invalid API key!")

        val error = expectFailure { repository.searchMovies("A Movie").asSnapshot() }

        assertTrue(error.message.orEmpty().contains("OMDB_API_KEY"))
    }

    @Test
    fun `quota failure remains retryable instead of an empty page`() = runTest(timeout = 5.seconds) {
        dataSource.response = failure("Request limit reached!")

        val error = expectFailure { repository.searchMovies("A Movie").asSnapshot() }

        assertTrue(error.message.orEmpty().contains("limit"))
    }

    @Test
    fun `other API errors ask the user to refine the query`() = runTest(timeout = 5.seconds) {
        dataSource.response = failure("Too many results.")

        val error = expectFailure { repository.searchMovies("Movie").asSnapshot() }

        assertTrue(error.message.orEmpty().contains("Too many results"))
        assertTrue(error.message.orEmpty().contains("refine your search"))
    }

    @Test
    fun `missing response status is an unreadable response`() = runTest(timeout = 5.seconds) {
        dataSource.response = MoviePageDto()

        val error = expectFailure { repository.searchMovies("A Movie").asSnapshot() }

        assertTrue(error.message.orEmpty().contains("unreadable response"))
    }

    @Test
    fun `blank search fails before requesting the network`() = runTest(timeout = 5.seconds) {
        val error = expectFailure { repository.searchMovies("  \n  ").asSnapshot() }

        assertTrue(error.message.orEmpty().contains("movie title"))
        assertFalse(dataSource.called)
    }

    @Test
    fun `connection failure preserves its cause in the paging error`() = runTest(timeout = 5.seconds) {
        val failure = IOException("No connection")
        dataSource.handler = { _, _ -> throw failure }

        val error = expectFailure { repository.searchMovies("A Movie").asSnapshot() }

        assertTrue(error.message.orEmpty().contains("connection"))
        assertTrue(generateSequence<Throwable>(error) { it.cause }.any { it === failure })
    }

    @Test
    fun `HTTP authentication failure explains the API key problem`() = runTest(timeout = 5.seconds) {
        val failure = HttpException(Response.error<MoviePageDto>(401, "{}".toResponseBody()))
        dataSource.handler = { _, _ -> throw failure }

        val error = expectFailure { repository.searchMovies("A Movie").asSnapshot() }

        assertTrue(error.message.orEmpty().contains("API key"))
        assertTrue(generateSequence<Throwable>(error) { it.cause }.any { it === failure })
    }

    @Test
    fun `malformed response becomes a paging error`() = runTest(timeout = 5.seconds) {
        val failure = JsonSyntaxException("Unexpected response")
        dataSource.handler = { _, _ -> throw failure }

        val error = expectFailure { repository.searchMovies("A Movie").asSnapshot() }

        assertTrue(error.message.orEmpty().contains("unreadable response"))
        assertTrue(generateSequence<Throwable>(error) { it.cause }.any { it === failure })
    }

    @Test
    fun `unconfigured datasource fails before calling OMDb`() = runTest(timeout = 5.seconds) {
        val api = object : MovieApi {
            override suspend fun searchMovies(query: String, page: Int, type: String): MoviePageDto =
                throw AssertionError("API must not be called without a key")
        }
        val unconfiguredRepository = OmdbMovieRepository(RetrofitMovieNetworkDataSource(api, false))

        val error = expectFailure { unconfiguredRepository.searchMovies("A Movie").asSnapshot() }

        assertTrue(error.message.orEmpty().contains("OMDB_API_KEY"))
    }

    private suspend fun expectFailure(block: suspend () -> Unit): MovieRepositoryException {
        try {
            block()
        } catch (exception: MovieRepositoryException) {
            return exception
        }
        throw AssertionError("Expected MovieRepositoryException")
    }

    private fun failure(message: String) = MoviePageDto(response = "False", error = message)

    private class FakeMovieNetworkDataSource : MovieNetworkDataSource {
        var response = MoviePageDto(results = emptyList(), totalResults = "0", response = "True")
        var handler: (suspend (String, Int) -> MoviePageDto)? = null
        val requests = mutableListOf<Pair<String, Int>>()
        val called get() = requests.isNotEmpty()

        override suspend fun searchMovies(query: String, page: Int): MoviePageDto {
            requests += query to page
            return handler?.invoke(query, page) ?: response
        }
    }

    private fun pageResponse(page: Int, total: Int): MoviePageDto = MoviePageDto(
        results = (((page - 1) * 10 + 1)..minOf(page * 10, total)).map { index ->
            MovieDto("tt$index", "Movie $index", "2020", null)
        },
        totalResults = total.toString(),
        response = "True",
    )
}
