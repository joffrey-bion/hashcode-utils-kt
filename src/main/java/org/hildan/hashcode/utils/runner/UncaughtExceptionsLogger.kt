package org.hildan.hashcode.utils.runner

import org.slf4j.LoggerFactory

/**
 * Defines what to do with the stack traces of exceptions that are thrown during parallel tasks execution.
 */
enum class UncaughtExceptionsLogger(
    private val printOnStdErr: Boolean,
    private val logViaSlf4J: Boolean
) {
    /**
     * Does nothing with the stack traces.
     *
     * Warning: when using this policy, the caller of the [HCRunner] will not be aware of uncaught exceptions
     * thrown within the solver. This policy should only be used when the solver is guaranteed not to throw any
     * exception (for instance if it wraps everything in a try/catch block)
     */
    NOOP(false, false),

    /**
     * Print uncaught exceptions' stack traces to the standard error stream.
     */
    STDERR(true, false),

    /**
     * Log exceptions via SLF4J logging API. This requires to provide an SLF4J implementation on the classpath,
     * otherwise nothing will be printed at all.
     */
    SLF4J(false, true),

    /**
     * Does both [STDERR] and [SLF4J].
     */
    STDERR_AND_SLF4J(true, true);

    fun log(message: String) {
        if (logViaSlf4J) {
            logger.error(message)
        }
        if (printOnStdErr) {
            System.err.println(message)
        }
    }

    fun log(message: String, t: Throwable) {
        if (logViaSlf4J) {
            logger.error(message, t)
        }
        if (printOnStdErr) {
            System.err.println(message)
            t.printStackTrace()
        }
    }

    companion object {

        private val logger by lazy { LoggerFactory.getLogger(UncaughtExceptionsLogger::class.java) }
    }
}
