package org.hildan.hashcode.utils.parser.context

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.io.Reader
import java.lang.IllegalArgumentException
import kotlin.test.assertFails
import kotlin.test.assertTrue

class ParsingContextTest {

    private lateinit var ctx: ParsingContext

    private fun context(inputText: String) = ParsingContext(inputText.reader())

    @Before
    fun setUp() {
        ctx = context("this is a test\n42 43 -44\n\nsomething\n")
    }

    @Test
    fun lineNumber() {
        ctx = context("this is a test\n42 43 -44\n\nsomething\n")
        assertEquals(0, ctx.lineNumber)
        ctx.nextLine()
        assertEquals(1, ctx.lineNumber)
        ctx.nextInt()
        assertEquals(2, ctx.lineNumber)
        ctx.nextInt()
        ctx.nextInt()
        assertEquals(2, ctx.lineNumber)
        ctx.nextString()
        assertEquals(4, ctx.lineNumber)
    }

    @Test
    fun skip() {
        ctx = context("this is a test\n42 43 -44\n\nsomething\n")
        assertEquals("this", ctx.nextString())
        ctx.skip() // "is"
        assertEquals("a", ctx.nextString())
        ctx.skip(2) // "test", "42"
        assertEquals("43", ctx.nextString())
        ctx.skip(0) // doesn't skip anything
        assertEquals(-44, ctx.nextInt())
        assertEquals("", ctx.nextLine())
        ctx.skip() // "something"
        val e = assertFails { ctx.skip() }
        assertTrue(e is NoMoreLinesToReadException)
    }

    @Test
    fun `skip() fails on negative param`() {
        val ctx = context("hello world")
        val e = assertFails { ctx.skip(-1) }
        assertTrue(e is IllegalArgumentException)
    }

    @Test
    fun `can skip() first element`() {
        val ctx = context("hello world")
        ctx.skip() // "hello"
        assertEquals("world", ctx.nextString())
    }

    @Test
    fun nextLine() {
        assertEquals("this is a test", ctx.nextLine())
        assertEquals("42 43 -44", ctx.nextLine())
        assertEquals("", ctx.nextLine())
        assertEquals("something", ctx.nextLine())
    }

    @Test
    fun `nextLine() should fail when no more lines to read`() {
        val ctx = context("this is the first line\nand the second")
        assertEquals("this is the first line", ctx.nextLine())
        assertEquals("and the second", ctx.nextLine())
        val e = assertFails { ctx.nextLine() }
        assertEquals(NoMoreLinesToReadException::class, e::class)
    }

    @Test
    fun `nextLine() should fail on IOException`() {
        val ctx = ParsingContext(FailingReader())
        val e = assertFails { ctx.nextLine() }
        assertTrue(e is InputParsingException)
    }

    @Test
    fun `nextString() and nextInt() standard case`() {
        val input = "this is a test\n42 43 -44\n\nsomething\n"
        val ctx = ParsingContext(input.reader())
        assertEquals("this", ctx.nextString())
        assertEquals("is", ctx.nextString())
        assertEquals("a", ctx.nextString())
        assertEquals("test", ctx.nextString())
        assertEquals(42, ctx.nextInt())
        assertEquals(43, ctx.nextInt())
        assertEquals(-44, ctx.nextInt())
        assertEquals("something", ctx.nextString())
    }

    @Test
    fun `nextInt() should fail on strings`() {
        val ctx = context("hello world")
        val e = assertFails { ctx.nextInt() }
        assertTrue(e is InputParsingException)
    }

    @Test
    fun `nextDouble() standard case`() {
        val ctx = context("1.0 5.5 -1.321")
        assertEquals(1.0, ctx.nextDouble(), 0.000001)
        assertEquals(5.5, ctx.nextDouble(), 0.000001)
        assertEquals(-1.321, ctx.nextDouble(), 0.000001)
    }

    @Test
    fun `nextDouble() should work on ints`() {
        val ctx = context("2 34 -21")
        assertEquals(2.0, ctx.nextDouble(), 0.000001)
        assertEquals(34.0, ctx.nextDouble(), 0.000001)
        assertEquals(-21.0, ctx.nextDouble(), 0.000001)
    }

    @Test
    fun `nextDouble() should fail on strings`() {
        val ctx = context("hello world")
        val e = assertFails { ctx.nextDouble() }
        assertTrue(e is InputParsingException)
    }

    @Test
    fun `nextLineAsIntArray() and nextLineAsStringArray()`() {
        val ctx = context("this is a test\n42 43 -44\n\nsomething\n")
        assertArrayEquals(arrayOf("this", "is", "a", "test"), ctx.nextLineAsStringArray())
        assertArrayEquals(intArrayOf(42, 43, -44), ctx.nextLineAsIntArray())
        assertArrayEquals(emptyArray(), ctx.nextLineAsStringArray())
        assertArrayEquals(arrayOf("something"), ctx.nextLineAsStringArray())
    }

    @Test
    fun `nextLineAsIntList() and nextLineAsStringList()`() {
        val ctx = context("this is a test\n42 43 -44\n\nsomething\n")
        assertEquals(listOf("this", "is", "a", "test"), ctx.nextLineAsStringList())
        assertEquals(listOf(42, 43, -44), ctx.nextLineAsIntList())
        assertEquals(emptyList<String>(), ctx.nextLineAsStringList())
        assertEquals(listOf("something"), ctx.nextLineAsStringList())
    }

    @Test
    fun `nextLineAsStringArray() should fail on incomplete line read`() {
        val ctx = context("this is the first line\nand the second")
        assertEquals("this", ctx.nextString())
        assertEquals("is", ctx.nextString())
        val e = assertFails { ctx.nextLineAsStringArray() }
        assertTrue(e is IncompleteLineReadException)
        assertTrue(e.message!!.contains("the first line"))
    }

    @Test
    fun `close() standard case`() {
        val ctx = context("hello world")
        assertEquals("hello world", ctx.nextLine())
        ctx.close()
    }

    @Test
    fun `close() should fail on unconsumed input`() {
        val ctx = context("hello world")
        val e = assertFails { ctx.close() }
        assertTrue(e is IncompleteInputReadException)
    }

    @Test
    fun `close() should fail on IOException`() {
        val ctx = ParsingContext(FailingReader())
        val e = assertFails { ctx.close() }
        assertTrue(e is InputParsingException)
    }

    private class FailingReader : Reader() {
        override fun read(cbuf: CharArray, off: Int, len: Int): Int = throw IOException("test exception")
        override fun close(): Unit = throw IOException("test exception")
    }
}
