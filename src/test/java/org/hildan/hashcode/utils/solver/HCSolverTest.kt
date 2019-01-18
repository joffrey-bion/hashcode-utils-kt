package org.hildan.hashcode.utils.solver

import org.hildan.hashcode.utils.parser.HCParser
import org.hildan.hashcode.utils.parser.context.ParsingContext
import org.junit.AfterClass
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

class HCSolverTest {

    private var solver: HCSolver<Input>? = null

    private class Input {
        var num: Int = 0
        var items: Array<String>? = null
    }

    @Before
    fun setUp() {
        val parser = HCParser { readInput() }
        solver = HCSolver(parser) { input -> input.items!!.map { s -> s + input.num } }
    }

    private fun ParsingContext.readInput(): Input {
        val input = Input()
        input.num = nextInt()
        input.items = nextLineAsStringArray()
        return input
    }

    @Test
    fun solve_success() {
        val lines = solver!!.solve(TEST_FILENAME) as List<String>
        assertEquals(3, lines.size.toLong())
        assertEquals("abc42", lines[0])
        assertEquals("def42", lines[1])
        assertEquals("ghi42", lines[2])
    }

    @Test
    @Throws(IOException::class)
    fun accept_success() {
        solver!!.accept(TEST_FILENAME)
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

        private val TEST_FILENAME = "testfile.in"

        private val EXPECTED_OUTPUT_FILENAME = "testfile.out"

        private val fileContent = "42\nabc def ghi"

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
