# Google HashCode utils for Kotlin
[![Bintray](https://img.shields.io/bintray/v/joffrey-bion/maven/hashcode-utils-kt.svg?colorB=blue&style=flat&label=jcenter)](https://bintray.com/joffrey-bion/maven/hashcode-utils-kt/_latestVersion)
[![Maven central version](https://img.shields.io/maven-central/v/org.hildan.hashcode/hashcode-utils-kt.svg)](http://mvnrepository.com/artifact/org.hildan.hashcode/hashcode-utils-kt)
[![Github Build](https://img.shields.io/github/workflow/status/joffrey-bion/hashcode-utils-kt/CI%20Build?label=build&logo=github)](https://github.com/joffrey-bion/hashcode-utils-kt/actions?query=workflow%3A%22CI+Build%22)
[![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)](https://github.com/joffrey-bion/hashcode-utils-kt/blob/master/LICENSE)

This library provides useful tools to make your life easier when competing in the Google Hash Code:
- A reader to easily map the input file to your classes representing the problem
- A writer to deal with the output file I/O and let you focus on the problem
- A runner that takes care of solving each input file in a separate coroutine with proper exception logging

The goal here is to take care of the boilerplate code to avoid debugging your input parser while you should be focusing
on solving the problem at hand.

## Example usage on past editions

You can find examples of usage of this library on previous HashCode editions problems in the
[examples folder](src/test/kotlin/org/hildan/hashcode/utils/examples).

For the purpose of this readme, we'll just give a quick glance at what this library provides, through a very simple
example problem.

## Simple example problem

Imagine the problem is to group points into clusters from a cloud of points. The input file gives you the number of 
points and the number of clusters to find, and then the list of point positions:

```
3 2      // 3 points, 2 clusters to find
1.2 4.2  // point 0: x=1.2 y=4.2
1.5 3.4  // point 1: x=1.5 y=3.4
6.8 2.2  // point 2: x=6.8 y=2.2
```

Now, let's assume you represent the problem this way:

```kotlin
data class Point(val x: Double, val y: Double)

data class Problem(
    val nClusters: Int,
    val points: List<Point>
) {
    fun solve(): List<String> {

        // solve the problem here

        // write solution into lines (this is problem-specific)
        val lines = mutableListOf<String>()
        lines.add("output line 0")
        lines.add("output line 1")
        return lines
    }
}
```

All of this is really what you want to be focusing on during the HashCode. We'll see how HashCode Utils can help you
with the rest.

### The input reader

The first thing you need is to read the input file and populate your model classes with the problem's data.
For our little example problem, here's how you would parse the input using HashCode Utils:

```kotlin
import java.nio.file.Paths
import org.hildan.hashcode.utils.reader.withHCReader

fun main(args: Array<String>) {
    val inputPath = Paths.get("problem.in")
    val problem = withHCReader(inputPath) { readProblem() }
    
    // solve the problem and write the output
}

private fun HCReader.readProblem(): Problem {
    val nPoints = readInt()
    val nClusters = readInt()
    val points = List(nPoints) { readPoint() }
    return Problem(nClusters, points)
}

private fun HCReader.readPoint(): Point {
    val x = readDouble()
    val y = readDouble()
    return Point(x, y)
}
```

The function `withHCReader` provides you with an instance of `HCReader` that you can use to read tokens from the 
input. `HCReader` provides primitives like `readInt`, `readString`, `readDouble`, as well as error handling with line 
numbers, which saves a lot of time.
 
To make the most of it, you should declare your own functions as extensions of `HCReader` so that it reads pretty 
neatly.

You may read more about the API directly in [HCReader](src/main/kotlin/org/hildan/hashcode/reader/HCReader.java)'s
 Javadocs.

### The output writer

The top-level function `writeHCOutputFile(path, lines)` allows you to write your output lines to a given file.

Outputting to the console and redirecting to a file could be a solution, but it prevents you from logging other stuff 
and monitoring what's going on in order to stop wasting time if things go out of hand.

### Read + write

In fact, you can do all of the above with a single function call, using `solveHCProblemAndWriteFile`:

```kotlin
import org.hildan.hashcode.utils.solveHCProblemAndWriteFile

fun main(args: Array<String>) {
    val inputPath = Paths.get("problem1.in")
    val outputPath = Paths.get("problem1.out")
    solveHCProblemAndWriteFile(inputPath, outputPath) { // this: HCReader
        readProblem().solve()
    }
}

// readProblem() declaration and model classes are unchanged
```

The `readAndSolve` lambda can use the provided `HCReader` like in the previous example, and needs to return the lines
to write to the output file, as an `Iterable<CharSequence>` (e.g. `List<String>`).

The output path is actually optional and can be automatically computed from the input by replacing `.in` by `.out` 
and replacing the `inputs/` directory, if present, by `outputs/`. The opinionated approach here is to place all input
files from the problem statement in an `inputs` directory, so that you get all the output in the `outputs` 
directory, ready to be uploaded.

### The runner

When you reach the end of the round, you might want to quickly run your program on all inputs at the same time using 
multiple threads in order to benefit from whatever improvement you make on any problem.

Let's assume you pass all input filenames as command line arguments. Then you can simply write:

```kotlin
import kotlinx.coroutines.runBlocking
import org.hildan.hashcode.utils.reader.HCReader
import org.hildan.hashcode.utils.solveHCFilesInParallel

fun main(args: Array<String>) = runBlocking {
    solveHCFilesInParallel(*args) { // this: HCReader
        readProblem().solve()
    }
}

// readProblem() declaration and model classes are unchanged
```

This combines all 3 features to make the most of this library. Just like before, you get an `HCReader` as receiver 
for your lambda in order to read the input, and your lambda must return the output lines as an `Iterable<CharSequence>`.

Happy HashCode!

## Add the dependency

Gradle dependency:

```kotlin
dependencies {
    compile("org.hildan.hashcode:hashcode-utils-kt:$version")
}
```

## License

Code released under the [MIT license](LICENSE).
