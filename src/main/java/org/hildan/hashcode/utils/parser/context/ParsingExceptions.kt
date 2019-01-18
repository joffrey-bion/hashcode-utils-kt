package org.hildan.hashcode.utils.parser.context

/**
 * Thrown if the parsing of the input fails.
 */
open class InputParsingException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
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
