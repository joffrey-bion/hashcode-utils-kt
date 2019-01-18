package org.hildan.hashcode.utils.parser

import org.hildan.hashcode.utils.parser.context.ParsingContext
import java.io.FileReader
import java.io.Reader
import java.io.StringReader

/**
 * [HCParser] provides convenient methods to parse the input as a [T] object. This is useful to convert Hash Code's
 * input file into an object representation of the problem.
 *
 * @param <T> the type of objects this parser yields
 */
class HCParser<T>(
    private val tokenDelimiter: Regex = Regex("\\s"),
    private val readInput: ParsingContext.() -> T
) {
    /**
     * Reads an instance of [T] from the file with the given [filename].
     */
    fun parseFile(filename: String): T = parse(FileReader(filename))

    /**
     * Reads an instance of [T] from the given [text].
     */
    fun parse(text: String): T = parse(StringReader(text))

    /**
     * Reads an instance of [T] from the given [inputReader].
     */
    fun parse(inputReader: Reader): T = parse(ParsingContext(inputReader, tokenDelimiter))

    /**
     * Reads an instance of [T] from the given [ParsingContext].
     */
    private fun parse(context: ParsingContext): T = context.use { it.readInput() }
}
