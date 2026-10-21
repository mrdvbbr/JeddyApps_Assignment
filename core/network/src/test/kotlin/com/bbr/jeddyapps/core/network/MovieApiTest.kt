package com.bbr.jeddyapps.core.network

import java.util.concurrent.TimeUnit
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class MovieApiTest {
    @Test
    fun `search sends OMDb parameters and parses its response`() = runTest {
        val server = MockWebServer()
        server.start()
        try {
            server.enqueue(
                MockResponse().setHeader("Content-Type", "application/json").setBody(
                    """
                    {
                      "Search": [{"Title": "Star Wars", "Year": "1977", "imdbID": "tt0076759", "Poster": "N/A"}],
                      "totalResults": "24",
                      "Response": "True"
                    }
                    """.trimIndent(),
                ),
            )
            val api = MovieApiFactory.create(apiKey = "test-key", baseUrl = server.url("/").toString())

            val response = api.searchMovies(query = "Star Wars", page = 2)
            val request = server.takeRequest(1, TimeUnit.SECONDS)

            assertNotNull(request)
            val url = requireNotNull(request?.requestUrl)
            assertEquals("/", url.encodedPath)
            assertEquals("test-key", url.queryParameter("apikey"))
            assertEquals("Star Wars", url.queryParameter("s"))
            assertEquals("2", url.queryParameter("page"))
            assertEquals("movie", url.queryParameter("type"))
            assertNull(request?.getHeader("Authorization"))
            assertEquals("True", response.response)
            assertEquals("24", response.totalResults)
            val movie = response.results.orEmpty().single()
            assertEquals("tt0076759", movie.id)
            assertEquals("Star Wars", movie.title)
            assertEquals("1977", movie.year)
            assertEquals("N/A", movie.posterUrl)
        } finally {
            server.shutdown()
        }
    }
}
