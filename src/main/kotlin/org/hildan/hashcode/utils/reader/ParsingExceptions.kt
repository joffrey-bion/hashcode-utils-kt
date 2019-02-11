package org.hildan.hashcode.utils.reader

import java.nio.file.Path

/**
 * Thrown if the parsing of the input fails.
 */
open class InputParsingException : RuntimeException {
    /**
     * Constructs a new InputParsingException with the given [message] and no cause.
     */
    constructor(message: String) : super(message)
    /**
     * Constructs a new InputParsingException with the given [message] and [cause].
     */
    constructor(message: String, cause: Throwable) : super(message, cause)
    /**
     * Constructs a new InputParsingException with the given [message], along with [lineNum] and [tokenNum] for easier
     * debugging.
     */
    constructor(lineNum: Int, tokenNum: Int, message: String) : super("Line $lineNum, token $tokenNum: \"$message\"")
}

/**
 * Thrown if the parsing ends while there is still input to read.
 */
class IncompleteInputReadException(nbLinesLeft: Int) :
    InputParsingException("The end of the input was not consumed, $nbLinesLeft lines remaining")

/**
 * Thrown if the next line is accessed while the current line has not been fully consumed.
 */
class IncompleteLineReadException(lineNum: Int, unreadEndOfLine: String) :
    InputParsingException("The end of line $lineNum was not consumed, remaining tokens: '$unreadEndOfLine'")

/**
 * Thrown if the input is accessed for more lines when there is no more to read.
 */
class NoMoreLinesToReadException : InputParsingException("End of input reached, cannot read more lines")

/**
 * Thrown if an exception occurs while parsing a file, in order to provide the file's path in the message.
 */
class FileParsingException(filePath: Path, cause: Exception):
    RuntimeException("Exception occurred while solving the input file '$filePath'", cause)
