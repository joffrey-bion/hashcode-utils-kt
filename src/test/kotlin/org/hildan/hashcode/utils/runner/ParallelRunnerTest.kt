package org.hildan.hashcode.utils.runner

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTimeout
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.Collections
import kotlin.test.assertEquals

internal class ParallelRunnerTest {

    @Test
    fun `runInParallel is actually parallel`() {
        assertTimeout(Duration.of(1, ChronoUnit.SECONDS)) {
            runBlocking {
                runInParallel("test1.in", "test2.in", "test3.in") {
                    delay(800)
                }
            }
        }
    }

    @Test
    fun `runInParallel runs a task on each input`() {
        val inputsRun = Collections.synchronizedSet(mutableSetOf<String>())
        runBlocking {
            runInParallel("test1.in", "test2.in", "test3.in") {
                inputsRun.add(it)
            }
        }
        assertEquals(setOf("test1.in", "test2.in", "test3.in"), inputsRun)
    }
}
