package org.hildan.hashcode.utils

import org.hildan.hashcode.utils.reader.HCReader
import org.hildan.hashcode.utils.reader.readHCInputFile
import org.hildan.hashcode.utils.runner.UncaughtExceptionsLogger
import org.hildan.hashcode.utils.runner.runInParallel
import org.hildan.hashcode.utils.writer.writeLinesToFile
import java.nio.file.Path
import java.nio.file.Paths

inline fun solveHCProblemAndWriteFile(
    inputFilePath: Path,
    outputFilePath: Path = computeOutputFilename(inputFilePath),
    readAndSolve: HCReader.() -> Iterable<CharSequence>
) {
    val outputLines = readHCInputFile(inputFilePath) { readAndSolve() }
    writeLinesToFile(outputFilePath, outputLines)
}

fun computeOutputFilename(inputPath: Path): Path {
    val normalized = inputPath.normalize()

    val parent: Path? = normalized.parent
    val fileName = normalized.fileName
    val newFileName = fileName.toString().removeSuffix(".in") + ".out"

    return when {
        parent == null -> Paths.get(newFileName)
        parent.endsWith("inputs") -> parent.resolveSibling("outputs").resolve(newFileName)
        else -> parent.resolve(newFileName)
    }
}

/**
 * Solves the problems defined by each of the given input files, each in its own coroutine.
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
        solveHCProblemAndWriteFile(Paths.get(it)) {
            readAndSolve()
        }
    }
}
