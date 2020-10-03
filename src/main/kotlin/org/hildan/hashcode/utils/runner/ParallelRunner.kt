package org.hildan.hashcode.utils.runner

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.Collections

/**
 * Runs the given function on each of the given input, each in its own coroutine.
 *
 * Note: Depending on your choice of [UncaughtExceptionsLogger], you may need to provide an SLF4J implementation
 * on your classpath to be able to see error logs.
 *
 * @param I the type of input that the block handles
 *
 * @param exceptionsLogger defines what to do with uncaught exceptions thrown by [block]
 * @param block the block to run on the inputs given to [run]
 */
suspend fun <I> runInParallel(
    vararg inputs: I,
    exceptionsLogger: UncaughtExceptionsLogger = UncaughtExceptionsLogger.STDERR,
    remindExceptionsAtTheEnd: Boolean = true,
    block: suspend (I) -> Unit
) {
    val exceptions = Collections.synchronizedList(mutableListOf<ExecException<I>>())
    coroutineScope {
        inputs.forEach {
            launch(Dispatchers.Default) {
                try {
                    block(it)
                } catch (e: Exception) {
                    exceptionsLogger.log("Uncaught exception thrown during task execution:", e)
                    exceptions.add(ExecException(it, e))
                }
            }
        }
    }
    if (exceptions.isNotEmpty() && remindExceptionsAtTheEnd) {
        remindExceptions(exceptions, exceptionsLogger)
    }
}

private fun <I> remindExceptions(exceptions: List<ExecException<I>>, exceptionsLogger: UncaughtExceptionsLogger) {
    exceptionsLogger.log("${exceptions.size} tasks terminated abruptly by throwing exceptions")
    exceptions.forEach {
        val message = "Reminder: this exception was thrown while running on input ${it.input}: ${it.exception.message}"
        exceptionsLogger.log(message, it.exception)
    }
}

private data class ExecException<I>(val input: I, val exception: Exception)
