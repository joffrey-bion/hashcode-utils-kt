package org.hildan.hashcode.utils.writer

import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeLines

/**
 * Writes the given [lines] to a file at the given [path]. All necessary parent directories are created if they don't
 * exist. The output file is overwritten if already existing.
 */
fun writeHCOutputFile(path: Path, lines: Iterable<CharSequence>) {
    try {
        path.parent?.createDirectories()
        path.writeLines(lines)
    } catch (e: Exception) {
        throw SolutionWritingException("Exception occurred while writing to the output file '$path'", e)
    }
}

/**
 * Thrown if an exception occurs while writing to the output file.
 */
class SolutionWritingException(msg: String, cause: Throwable) : Exception(msg, cause)
