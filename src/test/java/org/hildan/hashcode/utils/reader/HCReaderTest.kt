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
        hcr.nextLine()
        assertEquals(1, hcr.lineNumber)
        hcr.nextInt()
        assertEquals(2, hcr.lineNumber)
        hcr.nextInt()
        hcr.nextInt()
        assertEquals(2, hcr.lineNumber)
        hcr.nextString()
        assertEquals(4, hcr.lineNumber)
    }

    @Test
    fun skip() {
        hcr = hcReader("this is a test\n42 43 -44\n\nsomething\n")
        assertEquals("this", hcr.nextString())
        hcr.skip() // "is"
        assertEquals("a", hcr.nextString())
        hcr.skip(2) // "test", "42"
        assertEquals("43", hcr.nextString())
        hcr.skip(0) // doesn't skip anything
        assertEquals(-44, hcr.nextInt())
        assertEquals("", hcr.nextLine())
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
        assertEquals("world", hcr.nextString())
    }

    @Test
    fun nextLine() {
        assertEquals("this is a test", hcr.nextLine())
        assertEquals("42 43 -44", hcr.nextLine())
        assertEquals("", hcr.nextLine())
        assertEquals("something", hcr.nextLine())
    }

    @Test
    fun `nextLine() should fail when no more lines to read`() {
        val hcr = hcReader("this is the first line\nand the second")
        assertEquals("this is the first line", hcr.nextLine())
        assertEquals("and the second", hcr.nextLine())
        val e = assertFails { hcr.nextLine() }
        assertEquals(NoMoreLinesToReadException::class, e::class)
    }

    @Test
    fun `nextLine() should fail on IOException`() {
        val hcr = HCReader(FailingReader())
        val e = assertFails { hcr.nextLine() }
        assertTrue(e is InputParsingException)
    }

    @Test
    fun `nextString() and nextInt() standard case`() {
        val input = "this is a test\n42 43 -44\n\nsomething\n"
        val hcr = HCReader(input.reader())
        assertEquals("this", hcr.nextString())
        assertEquals("is", hcr.nextString())
        assertEquals("a", hcr.nextString())
        assertEquals("test", hcr.nextString())
        assertEquals(42, hcr.nextInt())
        assertEquals(43, hcr.nextInt())
        assertEquals(-44, hcr.nextInt())
        assertEquals("something", hcr.nextString())
    }

    @Test
    fun `nextInt() should fail on strings`() {
        val hcr = hcReader("hello world")
        val e = assertFails { hcr.nextInt() }
        assertTrue(e is InputParsingException)
    }

    @Test
    fun `nextDouble() standard case`() {
        val hcr = hcReader("1.0 5.5 -1.321")
        assertEquals(1.0, hcr.nextDouble(), 0.000001)
        assertEquals(5.5, hcr.nextDouble(), 0.000001)
        assertEquals(-1.321, hcr.nextDouble(), 0.000001)
    }

    @Test
    fun `nextDouble() should work on ints`() {
        val hcr = hcReader("2 34 -21")
        assertEquals(2.0, hcr.nextDouble(), 0.000001)
        assertEquals(34.0, hcr.nextDouble(), 0.000001)
        assertEquals(-21.0, hcr.nextDouble(), 0.000001)
    }

    @Test
    fun `nextDouble() should fail on strings`() {
        val hcr = hcReader("hello world")
        val e = assertFails { hcr.nextDouble() }
        assertTrue(e is InputParsingException)
    }

    @Test
    fun `nextLineAsIntArray() and nextLineAsStringArray()`() {
        val hcr = hcReader("this is a test\n42 43 -44\n\nsomething\n")
        assertArrayEquals(arrayOf("this", "is", "a", "test"), hcr.nextLineAsStringArray())
        assertArrayEquals(intArrayOf(42, 43, -44), hcr.nextLineAsIntArray())
        assertArrayEquals(emptyArray(), hcr.nextLineAsStringArray())
        assertArrayEquals(arrayOf("something"), hcr.nextLineAsStringArray())
    }

    @Test
    fun `nextLineAsIntList() and nextLineAsStringList()`() {
        val hcr = hcReader("this is a test\n42 43 -44\n\nsomething\n")
        assertEquals(listOf("this", "is", "a", "test"), hcr.nextLineAsStringList())
        assertEquals(listOf(42, 43, -44), hcr.nextLineAsIntList())
        assertEquals(emptyList<String>(), hcr.nextLineAsStringList())
        assertEquals(listOf("something"), hcr.nextLineAsStringList())
    }

    @Test
    fun `nextLineAsStringArray() should fail on incomplete line read`() {
        val hcr = hcReader("this is the first line\nand the second")
        assertEquals("this", hcr.nextString())
        assertEquals("is", hcr.nextString())
        val e = assertFails { hcr.nextLineAsStringArray() }
        assertTrue(e is IncompleteLineReadException)
        assertTrue(e.message!!.contains("the first line"))
    }

    @Test
    fun `close() standard case`() {
        val hcr = hcReader("hello world")
        assertEquals("hello world", hcr.nextLine())
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
