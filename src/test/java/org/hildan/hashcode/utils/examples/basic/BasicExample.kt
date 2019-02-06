package org.hildan.hashcode.utils.examples.basic

import kotlinx.coroutines.runBlocking
import org.hildan.hashcode.utils.reader.HCReader
import org.hildan.hashcode.utils.runner.solveInParallel
import java.util.ArrayList

fun main(args: Array<String>) = runBlocking {
    solveInParallel(*args) {
        readProblem().solve()
    }
}

private fun HCReader.readProblem(): Problem {
    val P = nextInt()
    val C = nextInt()
    val problem = Problem(C)
    val points = List(P) { readPoint() }
    problem.points = points
    return problem
}

private fun HCReader.readPoint(): Point {
    val x = nextDouble()
    val y = nextDouble()
    return Point(x, y)
}

internal class Point(val x: Double, val y: Double)

internal class Problem(val nClusters: Int) {

    var points: List<Point>? = null

    fun solve(): List<String> {

        // solve the problem here

        // write solution into lines (this is problem-specific)
        val lines = ArrayList<String>()
        lines.add("output line 0")
        lines.add("output line 1")
        return lines
    }
}
