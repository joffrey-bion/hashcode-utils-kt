package org.hildan.hashcode.utils.writer

import org.hildan.hashcode.utils.reader.HCReader
import org.junit.AfterClass
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

class HCWriterTest {

    private lateinit var writer: HCWriter

    private class Problem(
        var num: Int,
        var items: Array<String>
    ) {
        fun solve() = items.map { s -> s + num }
    }

    @Before
    fun setUp() {
        writer = HCWriter {
            readProblem().solve()
        }
    }

    private fun HCReader.readProblem(): Problem {
        val num = nextInt()
        val items = nextLineAsStringArray()
        return Problem(num, items)
    }

    @Test
    fun solveAndWrite() {
        writer.solveAndWrite(TEST_FILENAME)
        val outputFilePath = Paths.get(EXPECTED_OUTPUT_FILENAME)
        assertTrue(Files.exists(outputFilePath))
        val lines = Files.readAllLines(outputFilePath)
        assertEquals(3, lines.size.toLong())
        assertEquals("abc42", lines[0])
        assertEquals("def42", lines[1])
        assertEquals("ghi42", lines[2])
        Files.delete(Paths.get(EXPECTED_OUTPUT_FILENAME))
    }

    companion object {

        private const val TEST_FILENAME = "testfile.in"

        private const val EXPECTED_OUTPUT_FILENAME = "testfile.out"

        private const val fileContent = "42\nabc def ghi"

        @BeforeClass
        @JvmStatic
        fun createTestInputFile() {
            val path = Paths.get(TEST_FILENAME)
            Files.write(
                path,
                fileContent.toByteArray(),
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING
            )
        }

        @AfterClass
        @JvmStatic
        fun deleteTestInputFile() {
            Files.delete(Paths.get(TEST_FILENAME))
        }
    }
}
