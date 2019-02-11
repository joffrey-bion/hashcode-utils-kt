package org.hildan.hashcode.utils.writer

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class HCWriterTest {

    @ParameterizedTest
    @ValueSource(strings = ["testfile.out", "dir1/dir2/testfile.out"])
    fun writeLinesToFile(filename: String) {
        val outputFilePath = Paths.get(filename)
        val expectedLines = listOf("abc42", "def42", "ghi42")

        writeLinesToFile(outputFilePath, expectedLines)

        assertTrue(Files.exists(outputFilePath))
        assertEquals(expectedLines, Files.readAllLines(outputFilePath))
        Files.delete(outputFilePath)
    }

    @Test
    fun `writeLinesToFile fails on IOException`() {
        val outputPath = Paths.get("lockedFile.out")
        val lockedFile = outputPath.toFile()
        lockedFile.writeText("test content")
        lockedFile.setReadable(false)
        lockedFile.setWritable(false)

        val expectedLines = listOf("abc42", "def42", "ghi42")
        assertFailsWith<SolutionWritingException> { writeLinesToFile(outputPath, expectedLines) }

        lockedFile.delete()
    }
}
