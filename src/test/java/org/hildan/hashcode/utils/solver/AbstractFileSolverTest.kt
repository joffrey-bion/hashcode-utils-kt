package org.hildan.hashcode.utils.solver

import org.junit.experimental.theories.DataPoints
import org.junit.experimental.theories.Theories
import org.junit.experimental.theories.Theory
import org.junit.runner.RunWith

import org.junit.Assert.assertEquals

@RunWith(Theories::class)
class AbstractFileSolverTest {

    class Expectation(val input: String, val expectedOutput: String)

    @Theory
    fun computeOutputFilename(expectation: Expectation) {
        assertEquals(expectation.expectedOutput, computeOutputFilename(expectation.input))
    }

    companion object {

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
