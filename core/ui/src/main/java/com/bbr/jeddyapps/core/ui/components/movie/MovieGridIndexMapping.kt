package com.bbr.jeddyapps.core.ui.components.movie

/**
 * Maps final grid positions to paging indexes without allocating an entry for each movie.
 *
 * Inline positions are zero-based and include earlier inline entries. Negative positions and
 * positions beyond the reachable end of the grid are ignored. A position at the end appends a cell.
 */
internal class MovieGridIndexMapping(movieCount: Int, inlineIndexes: Set<Int>) {
    private val inlinePositions: IntArray
    val itemCount: Int

    init {
        require(movieCount >= 0) { "Movie count must not be negative" }
        inlinePositions = buildList {
            for (index in inlineIndexes.filter { it >= 0 }.sorted()) {
                if (index.toLong() <= movieCount.toLong() + size) add(index)
            }
        }.toIntArray()
        require(movieCount <= Int.MAX_VALUE - inlinePositions.size) { "Grid item count is too large" }
        itemCount = movieCount + inlinePositions.size
    }

    /** Returns null for an inline cell; all other positions retain their original paging index. */
    fun movieIndexAt(displayIndex: Int): Int? {
        require(displayIndex in 0 until itemCount) { "Display index is outside the grid" }
        val position = inlinePositions.binarySearch(displayIndex)
        return if (position >= 0) null else displayIndex - (-position - 1)
    }
}
