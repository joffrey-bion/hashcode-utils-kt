package org.hildan.hashcode.utils

import org.hildan.hashcode.utils.reader.HCReader
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.io.path.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
        val inputFilePath = Path("testfile.in")
        val outputFilePath = Path("testfile.out")
        inputFilePath.writeText("42\nabc def ghi")

        solveHCProblemAndWriteFile(inputFilePath) { readProblem().solve() }
        assertTrue(outputFilePath.exists())

        val lines = outputFilePath.readLines()
        val expectedLines = listOf("abc42", "def42", "ghi42")
        assertEquals(expectedLines, lines)

        inputFilePath.deleteExisting()
        outputFilePath.deleteExisting()
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
        assertEquals(Path(expectedOutput), computeHCOutputPath(Path(input)))
    }
}
