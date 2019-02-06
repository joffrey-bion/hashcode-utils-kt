package org.hildan.hashcode.utils.writer

import org.hildan.hashcode.utils.reader.HCReader
import org.hildan.hashcode.utils.reader.readHCInputFile
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths

private const val INPUT_EXTENSION = ".in"
private const val OUTPUT_EXTENSION = ".out"

private const val INPUT_FOLDER = "inputs/"
private const val OUTPUT_FOLDER = "outputs/"

class SolutionWritingException(msg: String, cause: Throwable) : Exception(msg, cause)

fun solveHCProblemAndPrint(inputFilename: String, readAndSolve: HCReader.() -> Iterable<CharSequence>) =
    readHCInputFile(inputFilename) { readAndSolve() }.forEach { println(it) }

fun solveHCProblemAndWriteFile(
    inputFilename: String,
    outputFilename: String = computeOutputFilename(inputFilename),
    readAndSolve: HCReader.() -> Iterable<CharSequence>
) {
    val outputLines = readHCInputFile(inputFilename) { readAndSolve() }
    try {
        writeOutputFile(outputFilename, outputLines)
    } catch (e: IOException) {
        throw SolutionWritingException("Exception occurred while writing to the output file '$outputFilename'", e)
    }
}

private fun writeOutputFile(outputFilename: String, lines: Iterable<CharSequence>) {
    val filePath = Paths.get(outputFilename)
    val parentDir = filePath.parent
    if (parentDir != null) {
        Files.createDirectories(parentDir)
    }
    Files.write(filePath, lines)
}

internal fun computeOutputFilename(inputFilename: String): String {
    val outputFilename = inputFilename
        .replace("^$INPUT_FOLDER".toRegex(), OUTPUT_FOLDER)
        .replace("/$INPUT_FOLDER".toRegex(), "/$OUTPUT_FOLDER")

    return if (outputFilename.endsWith(INPUT_EXTENSION)) {
        outputFilename.replace("$INPUT_EXTENSION$".toRegex(), OUTPUT_EXTENSION)
    } else {
        "$outputFilename$OUTPUT_EXTENSION"
    }
}
