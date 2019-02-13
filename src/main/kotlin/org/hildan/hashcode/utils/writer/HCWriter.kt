package org.hildan.hashcode.utils.writer

import java.nio.file.Files
import java.nio.file.Path

/**
 * Writes the given [lines] to a file at the given [path]. All necessary parent directories are created if they don't
 * exist. The output file is overwritten if already existing.
 */
fun writeHCOutputFile(path: Path, lines: Iterable<CharSequence>) {
    try {
        if (path.parent != null) {
            Files.createDirectories(path.parent)
        }
        Files.write(path, lines)
    } catch (e: Exception) {
        throw SolutionWritingException("Exception occurred while writing to the output file '$path'", e)
    }
}

/**
 * Thrown if an exception occurs while writing to the output file.
 */
class SolutionWritingException(msg: String, cause: Throwable) : Exception(msg, cause)
