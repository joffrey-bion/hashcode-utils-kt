package org.hildan.hashcode.utils.solver

import org.hildan.hashcode.utils.parser.HCParser
import org.hildan.hashcode.utils.parser.context.ParsingContext

interface Solvable {
    fun solve(): Iterable<CharSequence>
}

class SolverException(msg: String, cause: Throwable) : Exception(msg, cause)

fun <P : Solvable> solver(parseInput: ParsingContext.() -> P): HCSolver<P> = HCSolver(parseInput) { it.solve() }

class HCSolver<P>(
    private val problemParser: HCParser<P>,
    private val solve: (P) -> Iterable<CharSequence>
) : AbstractFileSolver() {

    constructor(parse: ParsingContext.() -> P, solve: (P) -> Iterable<CharSequence>): this(HCParser { parse() }, solve)

    override fun solve(inputFilename: String): Iterable<CharSequence> = try {
        solve(problemParser.parseFile(inputFilename))
    } catch (e: Exception) {
        throw SolverException("Exception occurred while parsing the input file '$inputFilename'", e)
    }
}
