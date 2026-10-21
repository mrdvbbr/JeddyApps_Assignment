package com.bbr.jeddyapps.data

import com.bbr.jeddyapps.core.network.MovieDto
import com.bbr.jeddyapps.core.network.MoviePageDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MovieMapperTest {
    @Test
    fun `missing and unavailable posters become null`() {
        listOf(null, "N/A", "", "  ").forEach { poster ->
            assertNull(MovieDto(id = "tt123", posterUrl = poster).toDomain()?.posterUrl)
        }
    }

    @Test
    fun `missing and duplicate IDs do not reach the movie list`() {
        val response = MoviePageDto(
            results = listOf(MovieDto(), MovieDto(id = "N/A"), MovieDto(id = "tt1"), MovieDto(id = "tt1")),
        )

        assertEquals(listOf("tt1"), response.toDomain(1).movies.map { it.id })
    }

    @Test
    fun `pagination rounds up API result count and caps the last page at one hundred`() {
        mapOf("0" to 0, "1" to 1, "10" to 1, "11" to 2, "1000" to 100, Long.MAX_VALUE.toString() to 100)
            .forEach { (count, expected) ->
                val page = MoviePageDto(totalResults = count).toDomain(3)
                assertEquals(3, page.page)
                assertEquals(expected, page.totalPages)
            }
    }

    @Test
    fun `missing title gets a readable fallback`() {
        assertEquals("Untitled movie", MovieDto(id = "tt1").toDomain()?.title)
    }
}
