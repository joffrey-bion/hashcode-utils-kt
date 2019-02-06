package org.hildan.hashcode.utils.runner

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.hildan.hashcode.utils.reader.HCReader
import org.hildan.hashcode.utils.writer.solveHCProblemAndWriteFile
import java.util.ArrayList

suspend fun solveHCFilesInParallel(
    vararg filenames: String,
    exceptionsLogger: UncaughtExceptionsLogger = UncaughtExceptionsLogger.STDERR,
    readAndSolve: HCReader.() -> Iterable<CharSequence>
) {
    runInParallel(*filenames, exceptionsLogger = exceptionsLogger) {
        solveHCProblemAndWriteFile(it, readAndSolve = readAndSolve)
    }
}

suspend fun <I> runInParallel(
    vararg inputs: I,
    exceptionsLogger: UncaughtExceptionsLogger = UncaughtExceptionsLogger.STDERR,
    block: (I) -> Unit
) {
    HCRunner(exceptionsLogger, block).run(*inputs)
}

private data class ExecException<I>(val input: I, val exception: Exception)

/**
 * `HCRunner` provides a simple way to execute a given solve on multiple inputs in separate parallel tasks.
 * Simply use [run(T...)][.run] or [run(int, T...)][.run] and you're good to go.
 *
 * Note: Depending on your choice of [UncaughtExceptionsLogger], you may need to provide an SLF4J implementation
 * on your classpath to be able to see error logs.
 *
 * @param I the type of input that the solve handles
 *
 * @param solve the solve to run on the inputs given to [run]
 * @param exceptionsLogger defines what to do with uncaught exceptions thrown by [solve]
 */
internal class HCRunner<I>(
    private val exceptionsLogger: UncaughtExceptionsLogger = UncaughtExceptionsLogger.STDERR,
    private val solve: (I) -> Unit
) {
    private val exceptions: MutableList<ExecException<I>> = ArrayList()

    /**
     * Executes the [solve] function given in the constructor on the given inputs, each in its own coroutine. This
     * method suspends until the execution on all inputs is complete.
     *
     * @param inputs the inputs to run [solve] on
     */
    @SafeVarargs
    suspend fun run(vararg inputs: I) {
        coroutineScope {
            inputs.forEach {
                launch(Dispatchers.Default) {
                    try {
                        solve(it)
                    } catch (e: Exception) {
                        exceptionsLogger.log("Uncaught exception thrown during task execution:", e)
                        exceptions.add(ExecException(it, e))
                    }
                }
            }
        }
        if (exceptions.isNotEmpty()) {
            remindExceptions()
        }
    }

    private fun remindExceptions() {
        exceptionsLogger.log("${exceptions.size} tasks terminated abruptly by throwing exceptions")
        exceptions.forEach {
            val message = "Reminder: this exception was thrown while running on input ${it.input}:"
            exceptionsLogger.log(message, it.exception)
        }
    }
}
