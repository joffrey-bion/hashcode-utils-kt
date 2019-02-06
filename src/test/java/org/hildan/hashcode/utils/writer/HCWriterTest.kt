package org.hildan.hashcode.utils.writer

import org.hildan.hashcode.utils.reader.HCReader
import org.junit.AfterClass
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.experimental.theories.DataPoints
import org.junit.experimental.theories.Theories
import org.junit.experimental.theories.Theory
import org.junit.runner.RunWith
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

@RunWith(Theories::class)
class HCWriterTest {

    private lateinit var writer: HCWriter

    private class Input {
        var num: Int = 0
        var items: Array<String>? = null
    }

    @Before
    fun setUp() {
        writer = HCWriter {
            readInput().let { input ->
                input.items!!.map { s -> s + input.num }
            }
        }
    }

    private fun HCReader.readInput(): Input {
        val input = Input()
        input.num = nextInt()
        input.items = nextLineAsStringArray()
        return input
    }

    @Test
    fun accept_success() {
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

    class Expectation(val input: String, val expectedOutput: String)

    @Theory
    fun computeOutputFilename(expectation: Expectation) {
        assertEquals(expectation.expectedOutput, computeOutputFilename(expectation.input))
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

        @JvmStatic
        @DataPoints
        fun createDataPoints(): Array<Expectation> {
            return arrayOf(
                Expectation("myInput", "myInput.out"),
                Expectation("myInput.in", "myInput.out"),
                Expectation("myInput.in.stuff", "myInput.in.stuff.out"),
                Expectation("inputs/myInput", "outputs/myInput.out"),
                Expectation("root/inputs/myInput", "root/outputs/myInput.out"),
                Expectation("root/inputsweird/myInput", "root/inputsweird/myInput.out"),
                Expectation("inputs/myInput.in", "outputs/myInput.out"),
                Expectation("weirdinputs/myInput", "weirdinputs/myInput.out"),
                Expectation("weirdinputs/myInput.in", "weirdinputs/myInput.out")
            )
        }
    }
}
