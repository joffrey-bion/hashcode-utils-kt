package org.hildan.hashcode.utils.writer

import org.hildan.hashcode.utils.reader.HCReader
import org.junit.AfterClass
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Paths

private const val TEST_FILENAME = "testfile.in"

private const val EXPECTED_OUTPUT_FILENAME = "testfile.out"

private const val fileContent = "42\nabc def ghi"

class HCWriterTest {

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
    fun solveAndWrite() {
        solveHCProblemAndWriteFile(TEST_FILENAME) { readProblem().solve() }
        val outputFilePath = Paths.get(EXPECTED_OUTPUT_FILENAME)
        assertTrue(Files.exists(outputFilePath))

        val lines = Files.readAllLines(outputFilePath)
        val expectedLines = listOf("abc42", "def42", "ghi42")
        assertEquals(expectedLines, lines)

        Files.delete(Paths.get(EXPECTED_OUTPUT_FILENAME))
    }

    companion object {

        @BeforeClass
        @JvmStatic
        fun createTestInputFile() {
            Files.write(Paths.get(TEST_FILENAME), fileContent.lines())
        }

        @AfterClass
        @JvmStatic
        fun deleteTestInputFile() {
            Files.delete(Paths.get(TEST_FILENAME))
        }
    }
}
