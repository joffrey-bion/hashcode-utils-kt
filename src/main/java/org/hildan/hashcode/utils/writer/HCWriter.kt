package org.hildan.hashcode.utils.writer

import org.hildan.hashcode.utils.reader.HCReader
import org.hildan.hashcode.utils.reader.readHashCodeInputFile
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

private const val INPUT_EXTENSION = ".in"
private const val OUTPUT_EXTENSION = ".out"

private const val INPUT_FOLDER = "inputs/"
private const val OUTPUT_FOLDER = "outputs/"

fun solveHashCodeProblem(
    inputFilename: String,
    outputFilename: String = computeOutputFilename(inputFilename),
    readAndSolve: HCReader.() -> Iterable<CharSequence>
) {
    HCWriter(readAndSolve).solveAndWrite(inputFilename, outputFilename)
}

class SolverException(msg: String, cause: Throwable) : Exception(msg, cause)

internal class HCWriter(
    private val readAndSolve: HCReader.() -> Iterable<CharSequence>
) {
    fun solveAndWrite(inputFilename: String, outputFilename: String = computeOutputFilename(inputFilename)) {
        val outputLines = solve(inputFilename)
        try {
            writeOutputFile(outputFilename, outputLines)
        } catch (e: IOException) {
            throw SolverException("Exception occurred while writing to the output file '$outputFilename'", e)
        }
    }

    private fun solve(inputFilename: String): Iterable<CharSequence> = try {
        readHashCodeInputFile(inputFilename) { readAndSolve() }
    } catch (e: Exception) {
        throw SolverException("Exception occurred while solving the input file '$inputFilename'", e)
    }

    private fun writeOutputFile(outputFilename: String, lines: Iterable<CharSequence>) {
        val filePath = Paths.get(outputFilename)
        val parentDir = filePath.parent
        if (parentDir != null) {
            Files.createDirectories(parentDir)
        }
        Files.write(
            filePath,
            lines,
            StandardCharsets.UTF_8,
            StandardOpenOption.CREATE,
            StandardOpenOption.WRITE,
            StandardOpenOption.TRUNCATE_EXISTING
        )
    }
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
