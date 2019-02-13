package org.hildan.hashcode.utils.reader

import java.io.Closeable
import java.io.IOException
import java.io.LineNumberReader
import java.io.Reader
import java.io.StringReader
import java.nio.file.Files
import java.nio.file.Path

/**
 * The most likely delimiter to use in HashCode input files.
 */
val DEFAULT_HASHCODE_INPUT_DELIMITER = Regex("""\s+""")

/**
 * Executes the given [readProblem] function on a new [HCReader], reading the given [input] text. The reader is closed
 * whether an exception is thrown or not.
 */
inline fun <P> withHCReader(
    input: String,
    tokenDelimiter: Regex = DEFAULT_HASHCODE_INPUT_DELIMITER,
    readProblem: HCReader.() -> P
): P = HCReader(StringReader(input), tokenDelimiter).use { it.readProblem() }

/**
 * Executes the given [readProblem] function on a new [HCReader], reading the file at the given [path]. The reader is
 * closed whether an exception is thrown or not.
 */
inline fun <P> withHCReader(
    path: Path,
    tokenDelimiter: Regex = DEFAULT_HASHCODE_INPUT_DELIMITER,
    readProblem: HCReader.() -> P
): P = try {
    HCReader(Files.newBufferedReader(path), tokenDelimiter).use { it.readProblem() }
} catch (e: Exception) {
    throw FileParsingException(path, e)
}

/**
 * Provides convenience methods to parse the input data, with clear error handling and line numbering.
 *
 * @param reader the reader to use to read the input
 * @param tokenDelimiter the delimiter to use to separate tokens
 */
class HCReader(reader: Reader, private val tokenDelimiter: Regex = DEFAULT_HASHCODE_INPUT_DELIMITER) : Closeable {

    private val reader: LineNumberReader = LineNumberReader(reader).apply { lineNumber = 0 }

    private var currentLineText: String = ""
    private var currentLineTokens: List<String> = emptyList()
    private var nextTokenIndex: Int = 0

    /**
     * The current line number. The value is 0 while nothing has been read yet, because the reader's position is
     * considered to be before line 1.
     */
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
        repeat(n) { nextToken() }
    }

    /**
     * Reads the next token of the input as a string.
     *
     * @throws NoMoreLinesToReadException if the last token of the line was consumed and there is no more lines to read
     * @throws InputParsingException if an error occurs while reading the input
     */
    fun readString(): String = nextToken()

    /**
     * Reads the next token of the input as an int.
     *
     * @throws NoMoreLinesToReadException if the last token of the line was consumed and there is no more lines to read
     * @throws InputParsingException if the input could not be parsed as an int
     */
    fun readInt(): Int = nextToken().let {
        it.toIntOrNull() ?: parseError("expected integer, got '$it'")
    }

    /**
     * Reads the next token of the input as a double.
     *
     * @throws NoMoreLinesToReadException if the last token of the line was consumed and there is no more lines to read
     * @throws InputParsingException if the input could not be parsed as a double
     */
    fun readDouble(): Double = nextToken().let {
        it.toDoubleOrNull() ?: parseError("expected double, got '$it'")
    }

    /**
     * Reads the next token of the input as a boolean.
     *
     * @throws NoMoreLinesToReadException if the last token of the line was consumed and there is no more lines to read
     * @throws InputParsingException if the input could not be parsed as a boolean
     */
    fun readBoolean(): Boolean = nextToken().let {
        when {
            it.equals("true", ignoreCase = true) -> true
            it.equals("false", ignoreCase = true) -> false
            else -> parseError("expected boolean, got '$it'")
        }
    }

    /**
     * Reads and returns the whole next line of input.
     *
     * @throws IncompleteLineReadException if the current line was not completely consumed
     * @throws NoMoreLinesToReadException if there is no more lines to read
     * @throws InputParsingException if an error occurs while reading the input
     */
    fun nextLineText(): String {
        fetchNextLine()
        // mark current line as consumed
        nextTokenIndex = currentLineTokens.size
        return currentLineText
    }

    /**
     * Reads and returns the next line of input as a list of strings.
     *
     * @throws IncompleteLineReadException if the current line was not completely consumed
     * @throws NoMoreLinesToReadException if there is no more lines to read
     * @throws InputParsingException if an error occurs while reading the input
     */
    fun nextLineTokens(): List<String> {
        fetchNextLine()
        // mark current line as consumed
        nextTokenIndex = currentLineTokens.size
        return currentLineTokens
    }

    private fun nextToken(): String {
        while (!hasMoreTokensInCurrentLine()) {
            fetchNextLine()
        }
        return currentLineTokens[nextTokenIndex++]
    }

    private fun hasMoreTokensInCurrentLine(): Boolean = nextTokenIndex < currentLineTokens.size

    private fun fetchNextLine() {
        try {
            if (hasMoreTokensInCurrentLine()) {
                throw IncompleteLineReadException(lineNumber, remainingInputOnCurrentLine())
            }
            val nextLine = reader.readLine() ?: throw NoMoreLinesToReadException()
            currentLineTokens = if (nextLine.isEmpty()) emptyList() else nextLine.split(tokenDelimiter)
            currentLineText = nextLine
            nextTokenIndex = 0
        } catch (e: IOException) {
            throw InputParsingException("An error occurred while reading the input line $lineNumber", e)
        }
    }

    private fun remainingInputOnCurrentLine(): String = remainingTokensOnCurrentLine().joinToString(" ")

    private fun remainingTokensOnCurrentLine(): List<String> =
        currentLineTokens.slice(nextTokenIndex until currentLineTokens.size)

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
