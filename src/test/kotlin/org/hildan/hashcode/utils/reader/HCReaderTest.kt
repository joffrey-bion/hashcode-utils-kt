package org.hildan.hashcode.utils.reader

import org.junit.jupiter.api.Test
import java.io.IOException
import java.io.Reader
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class HCReaderTest {

    private fun hcReader(inputText: String) = HCReader(inputText.reader())

    @Test
    fun lineNumber() {
        val hcr = hcReader("this is a test\n42 43 -44\n\nsomething\n")
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
        val hcr = hcReader("this is a test\n42 43 -44\n\nsomething\n")
        assertEquals("this", hcr.readString())
        hcr.skip() // "is"
        assertEquals("a", hcr.readString())
        hcr.skip(2) // "test", "42"
        assertEquals("43", hcr.readString())
        hcr.skip(0) // doesn't skip anything
        assertEquals(-44, hcr.readInt())
        assertEquals("", hcr.nextLineText())
        hcr.skip() // "something"
        assertFailsWith<NoMoreLinesToReadException> { hcr.skip() }
    }

    @Test
    fun `skip() fails on negative param`() {
        val hcr = hcReader("hello world")
        assertFailsWith<IllegalArgumentException> { hcr.skip(-1) }
    }

    @Test
    fun `can skip() first element`() {
        val hcr = hcReader("hello world")
        hcr.skip() // "hello"
        assertEquals("world", hcr.readString())
    }

    @Test
    fun `nextLineText() standard case`() {
        val hcr = hcReader("this is a test\n42 43 -44\n\nsomething\n")
        assertEquals("this is a test", hcr.nextLineText())
        assertEquals("42 43 -44", hcr.nextLineText())
        assertEquals("", hcr.nextLineText())
        assertEquals("something", hcr.nextLineText())
    }

    @Test
    fun `nextLineText() should fail when no more lines to read`() {
        val hcr = hcReader("this is the first line\nand the second")
        assertEquals("this is the first line", hcr.nextLineText())
        assertEquals("and the second", hcr.nextLineText())
        assertFailsWith<NoMoreLinesToReadException> { hcr.nextLineText() }
    }

    @Test
    fun `nextLineText() should fail on IOException`() {
        val hcr = HCReader(FailingReader())
        assertFailsWith<InputParsingException> { hcr.nextLineText() }
    }

    @Test
    fun `readString() and readInt() standard case`() {
        val hcr = hcReader("this is a test\n42 43 -44\n\nsomething\n")
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
    fun `readInt() should fail on non-numeric strings`() {
        val hcr = hcReader("hello world")
        assertFailsWith<InputParsingException> { hcr.readInt() }
    }

    @Test
    fun `readDouble() standard case`() {
        val hcr = hcReader("1.0 5.5 -1.321")
        assertEquals(1.0, hcr.readDouble())
        assertEquals(5.5, hcr.readDouble())
        assertEquals(-1.321, hcr.readDouble())
    }

    @Test
    fun `readDouble() should work on ints`() {
        val hcr = hcReader("2 34 -21")
        assertEquals(2.0, hcr.readDouble())
        assertEquals(34.0, hcr.readDouble())
        assertEquals(-21.0, hcr.readDouble())
    }

    @Test
    fun `readDouble() should fail on non-numeric strings`() {
        val hcr = hcReader("hello world")
        assertFailsWith<InputParsingException> { hcr.readDouble() }
    }

    @Test
    fun `readBoolean() standard case`() {
        val hcr = hcReader("true false\nTrue False\nTRUE FALSE")
        assertEquals(true, hcr.readBoolean())
        assertEquals(false, hcr.readBoolean())
        assertEquals(true, hcr.readBoolean())
        assertEquals(false, hcr.readBoolean())
        assertEquals(true, hcr.readBoolean())
        assertEquals(false, hcr.readBoolean())
    }

    @Test
    fun `readBoolean() should fail on non-boolean strings`() {
        val hcr = hcReader("true notABoolean")
        assertEquals(true, hcr.readBoolean())
        assertFailsWith<InputParsingException> { hcr.readBoolean() }
    }

    @Test
    fun `nextLineTokens() standard cases`() {
        val hcr = hcReader("this is a test\n42 43 -44\n\nsomething\n")
        assertEquals(listOf("this", "is", "a", "test"), hcr.nextLineTokens())
        assertEquals(listOf("42", "43", "-44"), hcr.nextLineTokens())
        assertEquals(emptyList(), hcr.nextLineTokens())
        assertEquals(listOf("something"), hcr.nextLineTokens())
    }

    @Test
    fun `nextLineTokens() should fail on incomplete line read`() {
        val hcr = hcReader("this is the first line\nand the second")
        assertEquals("this", hcr.readString())
        assertEquals("is", hcr.readString())
        val e = assertFailsWith<IncompleteLineReadException> { hcr.nextLineTokens() }
        assertTrue(e.message!!.contains("the first line"))
    }

    @Test
    fun `close() standard case`() {
        val hcr = hcReader("hello world")
        assertEquals("hello world", hcr.nextLineText())
        hcr.close()
    }

    @Test
    fun `close() nothing to read`() {
        hcReader("").close()
    }

    @Test
    fun `close() should fail on unconsumed input`() {
        val hcr = hcReader("hello world")
        assertFailsWith<IncompleteInputReadException> { hcr.close() }
    }

    @Test
    fun `close() should not fail on unconsumed blank lines`() {
        val hcr = hcReader("hello\n\n  \n")
        hcr.readString()
        hcr.close()
    }

    @Test
    fun `close() should fail on IOException`() {
        val hcr = HCReader(FailingReader())
        assertFailsWith<InputParsingException> { hcr.close() }
    }

    private class FailingReader : Reader() {
        override fun read(cbuf: CharArray, off: Int, len: Int): Int = throw IOException("test exception")
        override fun close(): Unit = throw IOException("test exception")
    }
}
