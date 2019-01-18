package org.hildan.hashcode.utils.parser

import org.hildan.hashcode.utils.parser.context.ParsingContext
import org.junit.Test

import org.junit.Assert.assertEquals

class HCParserTest {

    private val DELTA = 0.0001

    private val CONTENT = ( //
            "42 24 2\n" //
            + "first 3\n" //
            + "1.11 1.12\n" //
            + "1.21 1.22\n" //
            + "1.31 1.32\n" //
            + "second 2\n" //
            + "2.11 2.12\n" //
            + "2.21 2.22\n")

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

    private fun ParsingContext.readProblem(): Problem = Problem().apply {
        param1 = nextInt()
        param2 = nextInt()
        nShapes = nextInt()
        shapes = Array(nShapes) { readShape() }
    }

    private fun ParsingContext.readShape(): Shape = Shape().apply {
        name = nextString()
        nPoints = nextInt()
        points = List(nPoints) { readPoint() }
    }

    private fun ParsingContext.readPoint(): Point = Point().apply {
        x = nextDouble()
        y = nextDouble()
    }

    @Test
    fun test() {

        val hcParser = HCParser { readProblem() }
        val problem = hcParser.parse(CONTENT)

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
        assertEquals(1.11, point00.x, DELTA)
        assertEquals(1.12, point00.y, DELTA)

        val point01 = shape0.points[1]
        assertEquals(1.21, point01.x, DELTA)
        assertEquals(1.22, point01.y, DELTA)

        val point02 = shape0.points[2]
        assertEquals(1.31, point02.x, DELTA)
        assertEquals(1.32, point02.y, DELTA)

        val point10 = shape1.points!![0]
        assertEquals(2.11, point10.x, DELTA)
        assertEquals(2.12, point10.y, DELTA)

        val point11 = shape1.points[1]
        assertEquals(2.21, point11.x, DELTA)
        assertEquals(2.22, point11.y, DELTA)
    }
}
