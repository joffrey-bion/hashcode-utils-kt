package org.hildan.hashcode.utils.parser.context

import java.io.Closeable
import java.io.IOException
import java.io.LineNumberReader
import java.io.Reader

/**
 * Provides convenience methods to parse the input data, with clear error handling and line numbering.
 *
 * @param reader the reader to use to read the input
 * @param tokenDelimiter the delimiter to use to separate tokens
 */
class ParsingContext(reader: Reader, private val tokenDelimiter: Regex = Regex("\\s")) : Closeable {

    private val reader: LineNumberReader = LineNumberReader(reader).apply { lineNumber = 0 }

    private var currentLineString: String? = null
    private var currentLineTokens: Array<String>? = null
    private var nextTokenIndex: Int = 0

    val lineNumber: Int
      get() = reader.lineNumber

    /**
     * Skips the next [n] tokens of input.
     *
     * @throws NoMoreLinesToReadException if there is no more lines to read
     * @throws InputParsingException if an error occurs while reading the input
     */
    fun skip(n: Int = 1) {
        if (n < 0) {
            throw IllegalArgumentException("The number of elements to skip cannot be negative")
        }
        repeat(n) { nextString() }
    }

    /**
     * Reads the next token of the input as a string.
     *
     * @throws NoMoreLinesToReadException if the last token of the line was consumed and there is no more lines to read
     * @throws InputParsingException if an error occurs while reading the input
     */
    fun nextString(): String {
        while (!hasMoreTokenInCurrentLine()) {
            fetchNextLine()
        }
        return currentLineTokens!![nextTokenIndex++]
    }

    /**
     * Reads the next token of the input as an int.
     *
     * @throws NoMoreLinesToReadException if the last token of the line was consumed and there is no more lines to read
     * @throws InputParsingException if the input could not be parsed as an int
     */
    fun nextInt(): Int = nextString().let {
        it.toIntOrNull() ?: parseError("expected integer, got '$it'")
    }

    /**
     * Reads the next token of the input as a double.
     *
     * @throws NoMoreLinesToReadException if the last token of the line was consumed and there is no more lines to read
     * @throws InputParsingException if the input could not be parsed as an double
     */
    fun nextDouble(): Double = nextString().let {
        it.toDoubleOrNull() ?: parseError("expected double, got '$it'")
    }

    /**
     * Reads and returns the next line of input.
     *
     * @throws IncompleteLineReadException if the previous line was not completely consumed
     * @throws NoMoreLinesToReadException if there is no more lines to read
     * @throws InputParsingException if an error occurs while reading the input
     */
    fun nextLine(): String? {
        fetchNextLine()
        // mark current line as consumed
        nextTokenIndex = currentLineTokens!!.size
        return currentLineString
    }

    /**
     * Reads and returns the next line of input as an array of string tokens.
     *
     * @throws IncompleteLineReadException if the previous line was not completely consumed
     * @throws NoMoreLinesToReadException if there is no more lines to read
     * @throws InputParsingException if an error occurs while reading the input
     */
    fun nextLineAsStringArray(): Array<String> {
        fetchNextLine()
        // mark current line as consumed
        nextTokenIndex = currentLineTokens!!.size
        return currentLineTokens!!
    }

    /**
     * Reads and returns the next line of input as an array of ints.
     *
     * @throws IncompleteLineReadException if the previous line was not completely consumed
     * @throws NoMoreLinesToReadException if there is no more lines to read
     * @throws InputParsingException if an error occurs while reading the input or converting to int
     */
    fun nextLineAsIntArray(): IntArray = nextLineAsIntList().toIntArray()

    /**
     * Reads and returns the next line of input as a list of strings.
     *
     * @throws IncompleteLineReadException if the previous line was not completely consumed
     * @throws NoMoreLinesToReadException if there is no more lines to read
     * @throws InputParsingException if an error occurs while reading the input or converting to int
     */
    fun nextLineAsStringList(): List<String> = nextLineAsStringArray().toList()

    /**
     * Reads and returns the next line of input as a list of strings.
     *
     * @throws IncompleteLineReadException if the previous line was not completely consumed
     * @throws NoMoreLinesToReadException if there is no more lines to read
     * @throws InputParsingException if an error occurs while reading the input or converting to int
     */
    fun nextLineAsIntList(): List<Int> = nextLineAsStringArray().map { it.toInt() }

    private fun hasMoreTokenInCurrentLine(): Boolean = currentLineTokens != null && nextTokenIndex < currentLineTokens!!.size

    private fun fetchNextLine() {
        try {
            if (hasMoreTokenInCurrentLine()) {
                throw IncompleteLineReadException(lineNumber, remainingInputOnCurrentLine())
            }
            val nextLine = reader.readLine() ?: throw NoMoreLinesToReadException()
            currentLineTokens = if (nextLine.isEmpty()) emptyArray() else nextLine.split(tokenDelimiter).toTypedArray()
            currentLineString = nextLine
            nextTokenIndex = 0
        } catch (e: IOException) {
            throw InputParsingException("An error occurred while reading the input line $lineNumber", e)
        }
    }

    private fun remainingInputOnCurrentLine(): String = remainingTokens().joinToString(" ")

    private fun remainingTokens(): List<String> = currentLineTokens!!.slice(nextTokenIndex until currentLineTokens!!.size)

    /**
     * Releases potential resources used by the reader. Should be called when parsing is over.
     *
     * @throws IncompleteInputReadException if there is still some input left to read
     * @throws InputParsingException if an error occurs while consuming the end of the input
     */
    override fun close() {
        try {
            val nbLinesLeft = reader.useLines { it.count() }
            if (nbLinesLeft > 0) {
                throw IncompleteInputReadException(nbLinesLeft)
            }
        } catch (e: IOException) {
            throw InputParsingException("An error occurred while consuming the end of the input", e)
        }
    }

    private fun parseError(msg: String): Nothing = throw InputParsingException(lineNumber, nextTokenIndex, msg)
}
