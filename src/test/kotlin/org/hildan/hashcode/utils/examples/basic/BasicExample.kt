package org.hildan.hashcode.utils.examples.basic

import kotlinx.coroutines.runBlocking
import org.hildan.hashcode.utils.reader.HCReader
import org.hildan.hashcode.utils.runner.solveHCFilesInParallel
import java.util.ArrayList

fun main(args: Array<String>) = runBlocking {
    solveHCFilesInParallel(*args) {
        readProblem().solve()
    }
}

private fun HCReader.readProblem(): Problem {
    val P = readInt()
    val C = readInt()
    val points = List(P) { readPoint() }
    return Problem(C, points)
}

private fun HCReader.readPoint(): Point {
    val x = readDouble()
    val y = readDouble()
    return Point(x, y)
}

private data class Point(val x: Double, val y: Double)

private data class Problem(
    val nClusters: Int,
    val points: List<Point>
) {
    fun solve(): List<String> {

        // solve the problem here

        // write solution into lines (this is problem-specific)
        val lines = ArrayList<String>()
        lines.add("output line 0")
        lines.add("output line 1")
        return lines
    }
}
