package org.hildan.hashcode.utils.reader

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.io.Reader
import kotlin.test.assertFails
import kotlin.test.assertTrue

class HCReaderTest {

    private lateinit var hcr: HCReader

    private fun hcReader(inputText: String) = HCReader(inputText.reader())

    @Before
    fun setUp() {
        hcr = hcReader("this is a test\n42 43 -44\n\nsomething\n")
    }

    @Test
    fun lineNumber() {
        hcr = hcReader("this is a test\n42 43 -44\n\nsomething\n")
        assertEquals(0, hcr.lineNumber)
        hcr.nextLineText()
        assertEquals(1, hcr.lineNumber)
        hcr.readInt()
        assertEquals(2, hcr.lineNumber)
        hcr.readInt()
        hcr.readInt()
        assertEquals(2, hcr.lineNumber)
        hcr.readString()
        assertEquals(4, hcr.lineNumber)
    }

    @Test
    fun skip() {
        hcr = hcReader("this is a test\n42 43 -44\n\nsomething\n")
        assertEquals("this", hcr.readString())
        hcr.skip() // "is"
        assertEquals("a", hcr.readString())
        hcr.skip(2) // "test", "42"
        assertEquals("43", hcr.readString())
        hcr.skip(0) // doesn't skip anything
        assertEquals(-44, hcr.readInt())
        assertEquals("", hcr.nextLineText())
        hcr.skip() // "something"
        val e = assertFails { hcr.skip() }
        assertTrue(e is NoMoreLinesToReadException)
    }

    @Test
    fun `skip() fails on negative param`() {
        val hcr = hcReader("hello world")
        val e = assertFails { hcr.skip(-1) }
        assertTrue(e is IllegalArgumentException)
    }

    @Test
    fun `can skip() first element`() {
        val hcr = hcReader("hello world")
        hcr.skip() // "hello"
        assertEquals("world", hcr.readString())
    }

    @Test
    fun nextLine() {
        assertEquals("this is a test", hcr.nextLineText())
        assertEquals("42 43 -44", hcr.nextLineText())
        assertEquals("", hcr.nextLineText())
        assertEquals("something", hcr.nextLineText())
    }

    @Test
    fun `nextLine() should fail when no more lines to read`() {
        val hcr = hcReader("this is the first line\nand the second")
        assertEquals("this is the first line", hcr.nextLineText())
        assertEquals("and the second", hcr.nextLineText())
        val e = assertFails { hcr.nextLineText() }
        assertEquals(NoMoreLinesToReadException::class, e::class)
    }

    @Test
    fun `nextLine() should fail on IOException`() {
        val hcr = HCReader(FailingReader())
        val e = assertFails { hcr.nextLineText() }
        assertTrue(e is InputParsingException)
    }

    @Test
    fun `nextString() and nextInt() standard case`() {
        val input = "this is a test\n42 43 -44\n\nsomething\n"
        val hcr = HCReader(input.reader())
        assertEquals("this", hcr.readString())
        assertEquals("is", hcr.readString())
        assertEquals("a", hcr.readString())
        assertEquals("test", hcr.readString())
        assertEquals(42, hcr.readInt())
        assertEquals(43, hcr.readInt())
        assertEquals(-44, hcr.readInt())
        assertEquals("something", hcr.readString())
    }

    @Test
    fun `nextInt() should fail on strings`() {
        val hcr = hcReader("hello world")
        val e = assertFails { hcr.readInt() }
        assertTrue(e is InputParsingException)
    }

    @Test
    fun `nextDouble() standard case`() {
        val hcr = hcReader("1.0 5.5 -1.321")
        assertEquals(1.0, hcr.readDouble(), 0.000001)
        assertEquals(5.5, hcr.readDouble(), 0.000001)
        assertEquals(-1.321, hcr.readDouble(), 0.000001)
    }

    @Test
    fun `nextDouble() should work on ints`() {
        val hcr = hcReader("2 34 -21")
        assertEquals(2.0, hcr.readDouble(), 0.000001)
        assertEquals(34.0, hcr.readDouble(), 0.000001)
        assertEquals(-21.0, hcr.readDouble(), 0.000001)
    }

    @Test
    fun `nextDouble() should fail on strings`() {
        val hcr = hcReader("hello world")
        val e = assertFails { hcr.readDouble() }
        assertTrue(e is InputParsingException)
    }

    @Test
    fun `nextLineAsIntArray() and nextLineAsStringList()`() {
        val hcr = hcReader("this is a test\n42 43 -44\n\nsomething\n")
        assertEquals(listOf("this", "is", "a", "test"), hcr.nextLineTokens())
        assertArrayEquals(intArrayOf(42, 43, -44), IntArray(3) { hcr.readInt() })
        assertEquals(emptyList<String>(), hcr.nextLineTokens())
        assertEquals(listOf("something"), hcr.nextLineTokens())
    }

    @Test
    fun `nextLineAsIntList() and nextLineAsStringList()`() {
        val hcr = hcReader("this is a test\n42 43 -44\n\nsomething\n")
        assertEquals(listOf("this", "is", "a", "test"), hcr.nextLineTokens())
        assertEquals(listOf(42, 43, -44), List(3) { hcr.readInt() })
        assertEquals(emptyList<String>(), hcr.nextLineTokens())
        assertEquals(listOf("something"), hcr.nextLineTokens())
    }

    @Test
    fun `nextLineAsStringArray() should fail on incomplete line read`() {
        val hcr = hcReader("this is the first line\nand the second")
        assertEquals("this", hcr.readString())
        assertEquals("is", hcr.readString())
        val e = assertFails { hcr.nextLineTokens() }
        assertTrue(e is IncompleteLineReadException)
        assertTrue(e.message!!.contains("the first line"))
    }

    @Test
    fun `close() standard case`() {
        val hcr = hcReader("hello world")
        assertEquals("hello world", hcr.nextLineText())
        hcr.close()
    }

    @Test
    fun `close() should fail on unconsumed input`() {
        val hcr = hcReader("hello world")
        val e = assertFails { hcr.close() }
        assertTrue(e is IncompleteInputReadException)
    }

    @Test
    fun `close() should fail on IOException`() {
        val hcr = HCReader(FailingReader())
        val e = assertFails { hcr.close() }
        assertTrue(e is InputParsingException)
    }

    private class FailingReader : Reader() {
        override fun read(cbuf: CharArray, off: Int, len: Int): Int = throw IOException("test exception")
        override fun close(): Unit = throw IOException("test exception")
    }
}
