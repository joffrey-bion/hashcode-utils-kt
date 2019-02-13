package org.hildan.hashcode.utils.reader

import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

private val CONTENT = """
    42 24 2
    first 3
    1.11 1.12
    1.21 1.22
    1.31 1.32
    second 2
    2.11 2.12
    2.21 2.22
    """.trimIndent()

class HCReaderGlobalTest {

    private class Problem {
        var param1: Int = 0
        var param2: Int = 0
        var nShapes: Int = 0
        var shapes: Array<Shape> = emptyArray()
    }

    private class Shape {
        var name: String? = null
        var nPoints: Int = 0
        var points: List<Point> = emptyList()
    }

    private class Point {
        var x: Double = 0.0
        var y: Double = 0.0
    }

    private fun HCReader.readProblem(): Problem = Problem().apply {
        param1 = readInt()
        param2 = readInt()
        nShapes = readInt()
        shapes = Array(nShapes) { readShape() }
    }

    private fun HCReader.readShape(): Shape = Shape().apply {
        name = readString()
        nPoints = readInt()
        points = List(nPoints) { readPoint() }
    }

    private fun HCReader.readPoint(): Point = Point().apply {
        x = readDouble()
        y = readDouble()
    }

    @Test
    fun `withHCReader() on text`() {
        val problem = withHCReader(CONTENT) { readProblem() }
        verifyProblemData(problem)
    }

    @Test
    fun `withHCReader() on file`() {
        val filePath = Paths.get("testfile.in")
        Files.write(filePath, CONTENT.lines())

        val problem = withHCReader(filePath) { readProblem() }
        verifyProblemData(problem)

        Files.delete(filePath)
    }

    @Test
    fun `withHCReader() fails on IOException`() {
        val filePath = Paths.get("unknown.in")

        assertFailsWith<FileParsingException> {
            withHCReader(filePath) { readProblem() }
        }
    }

    private fun verifyProblemData(problem: Problem) {
        assertEquals(42, problem.param1.toLong())
        assertEquals(24, problem.param2.toLong())
        assertEquals(2, problem.nShapes.toLong())
        assertEquals(2, problem.shapes.size.toLong())

        val shape0 = problem.shapes[0]
        assertEquals("first", shape0.name)
        assertEquals(3, shape0.nPoints.toLong())

        val shape1 = problem.shapes[1]
        assertEquals("second", shape1.name)
        assertEquals(2, shape1.nPoints.toLong())

        val point00 = shape0.points[0]
        assertEquals(1.11, point00.x)
        assertEquals(1.12, point00.y)

        val point01 = shape0.points[1]
        assertEquals(1.21, point01.x)
        assertEquals(1.22, point01.y)

        val point02 = shape0.points[2]
        assertEquals(1.31, point02.x)
        assertEquals(1.32, point02.y)

        val point10 = shape1.points[0]
        assertEquals(2.11, point10.x)
        assertEquals(2.12, point10.y)

        val point11 = shape1.points[1]
        assertEquals(2.21, point11.x)
        assertEquals(2.22, point11.y)
    }
}
