package org.hildan.hashcode.utils.writer

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.assertEquals

class ComputeOutputFilenameTest {

    @ParameterizedTest(name = "''{0}'' gives ''{1}''")
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
        assertEquals(expectedOutput, computeOutputFilename(input))
    }
}
