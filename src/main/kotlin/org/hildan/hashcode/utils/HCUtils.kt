package org.hildan.hashcode.utils

import org.hildan.hashcode.utils.reader.HCReader
import org.hildan.hashcode.utils.reader.withHCReader
import org.hildan.hashcode.utils.runner.UncaughtExceptionsLogger
import org.hildan.hashcode.utils.runner.runInParallel
import org.hildan.hashcode.utils.writer.writeHCOutputFile
import java.nio.file.Path
import kotlin.io.path.*

/**
 * Reads the input file at [inputFilePath] by calling [readAndSolve], and writes the returned lines to the file at
 * [outputFilePath].
 *
 * @param inputFilePath the path to the input file
 * @param outputFilePath the path to the output file (will be overwritten if it exists). By default, the output file
 * is computed from the input filename using [computeHCOutputPath].
 * @param readAndSolve a function that reads the problem, solves it, and returns the output lines to write
 */
inline fun solveHCProblemAndWriteFile(
    inputFilePath: Path,
    outputFilePath: Path = computeHCOutputPath(inputFilePath),
    readAndSolve: HCReader.() -> Iterable<CharSequence>
) {
    val outputLines = withHCReader(inputFilePath) { readAndSolve() }
    writeHCOutputFile(outputFilePath, outputLines)
}

/**
 * Computes the output file path based on the input file path. If there is a `.in` extension, it is changed to `.out`,
 * otherwise `.out` is simply appended. If there is a parent folder called `inputs`, it is changed to `outputs`.
 */
fun computeHCOutputPath(inputPath: Path): Path {
    val normalized = inputPath.normalize()

    val parent: Path? = normalized.parent
    val newFileName = normalized.fileName.toString().removeSuffix(".in") + ".out"

    return when {
        parent == null -> Path(newFileName)
        parent.endsWith("inputs") -> parent.resolveSibling("outputs").resolve(newFileName)
        else -> parent.resolve(newFileName)
    }
}

/**
 * Solves the problems defined by each of the given input files, each in its own coroutine.
 *
 * @param filenames the paths to the input files to read
 * @param exceptionsLogger the way to log uncaught exceptions (defaults to standard error stream)
 * @param remindExceptionsAtTheEnd whether to remind all exceptions that occurred during the runs when all coroutines
 * are done (true by default to ease debugging when the output is too big)
 * @param readAndSolve a function to read the input and solve the problem, returning the output lines to write
 */
suspend fun solveHCFilesInParallel(
    vararg filenames: String,
    exceptionsLogger: UncaughtExceptionsLogger = UncaughtExceptionsLogger.STDERR,
    remindExceptionsAtTheEnd: Boolean = true,
    readAndSolve: suspend HCReader.() -> Iterable<CharSequence>
) {
    runInParallel(
        *filenames,
        exceptionsLogger = exceptionsLogger,
        remindExceptionsAtTheEnd = remindExceptionsAtTheEnd
    ) {
        solveHCProblemAndWriteFile(Path(it)) {
            readAndSolve()
        }
    }
}
