package org.hildan.hashcode.utils

import org.hildan.hashcode.utils.reader.HCReader
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.test.assertEquals

internal class HCUtilsTest {

    private class Problem(
        var num: Int,
        var items: List<String>
    ) {
        fun solve() = items.map { s -> s + num }
    }

    private fun HCReader.readProblem(): Problem {
        val num = readInt()
        val items = List(3) { readString() }
        return Problem(num, items)
    }

    @Test
    fun solveHCProblemAndWriteFile() {
        val inputFilePath = Paths.get("testfile.in")
        val outputFilePath = Paths.get("testfile.out")
        Files.write(inputFilePath, listOf("42", "abc def ghi"))

        solveHCProblemAndWriteFile(inputFilePath) { readProblem().solve() }
        kotlin.test.assertTrue(Files.exists(outputFilePath))

        val lines = Files.readAllLines(outputFilePath)
        val expectedLines = listOf("abc42", "def42", "ghi42")
        kotlin.test.assertEquals(expectedLines, lines)

        Files.delete(inputFilePath)
        Files.delete(outputFilePath)
    }

    @ParameterizedTest(name = "computeHCOutputPath(''{0}'') = ''{1}''")
    @CsvSource(
        value = [
            "myInput, myInput.out",
            "myInput.in, myInput.out",
            "myInput.in.stuff, myInput.in.stuff.out",
            "inputs/myInput, outputs/myInput.out",
            "root/inputs/myInput, root/outputs/myInput.out",
            "root/inputsweird/myInput, root/inputsweird/myInput.out",
            "inputs/myInput.in, outputs/myInput.out",
            "weirdinputs/myInput, weirdinputs/myInput.out",
            "weirdinputs/myInput.in, weirdinputs/myInput.out"
        ]
    )
    fun computeOutputFilename(input: String, expectedOutput: String) {
        assertEquals(Paths.get(expectedOutput), computeHCOutputPath(Paths.get(input)))
    }
}
