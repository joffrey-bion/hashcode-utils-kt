package org.hildan.hashcode.utils.writer

import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readLines
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class HCWriterTest {

    @TempDir
    lateinit var tempDirPath: Path

    @ParameterizedTest
    @ValueSource(strings = ["testfile.out", "dir1/dir2/testfile.out"])
    fun writeHCOutputFile(filename: String) {
        val outputFilePath = tempDirPath.resolve(filename)
        val expectedLines = listOf("abc42", "def42", "ghi42")

        writeHCOutputFile(outputFilePath, expectedLines)

        assertTrue(outputFilePath.exists())
        assertEquals(expectedLines, outputFilePath.readLines())
    }

    @Test
    fun `writeHCOutputFile fails on IOException`() {
        val outputPath = tempDirPath.resolve("lockedFile.out")
        val lockedFile = outputPath.toFile()
        lockedFile.writeText("test content")
        lockedFile.setReadable(false)
        lockedFile.setWritable(false)

        val expectedLines = listOf("abc42", "def42", "ghi42")
        assertFailsWith<SolutionWritingException> { writeHCOutputFile(outputPath, expectedLines) }

        lockedFile.delete()
    }
}
