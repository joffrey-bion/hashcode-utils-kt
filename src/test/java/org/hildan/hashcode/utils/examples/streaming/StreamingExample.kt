package org.hildan.hashcode.utils.examples.streaming

import org.hildan.hashcode.utils.reader.HCReader
import org.hildan.hashcode.utils.reader.readHashCodeInput
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class StreamingExample {

    private val input = ("5 2 4 3 100\n" // 5 videos, 2 endpoints, 4 request descriptions, 3 caches 100MB each
            + "50 50 80 30 110\n" // Videos 0, 1, 2, 3, 4 have sizes 50MB, 50MB, 80MB, 30MB, 110MB.
            + "1000 3\n" // Endpoint 0 has 1000ms datacenter latency and is connected to 3 caches:
            + "0 100\n"  // The latency (of endpoint 0) to cache 0 is 100ms.
            + "2 200\n"  // The latency (of endpoint 0) to cache 2 is 200ms.
            + "1 300\n"  // The latency (of endpoint 0) to cache 1 is 300ms.
            + "500 0\n"  // Endpoint 1 has 500ms datacenter latency and is not connected to a cache.
            + "3 0 1500\n" // 1500 requests for video 3 coming from endpoint 0.
            + "0 1 1000\n" // 1000 requests for video 0 coming from endpoint 1.
            + "4 0 500\n"  // 500 requests for video 4 coming from endpoint 0.
            + "1 0 1000")  // 1000 requests for video 1 coming from endpoint 0.

    private fun HCReader.readStreamingProblem(): StreamingProblem {
        val problem = StreamingProblem().apply {
            nVideos = nextInt()
            nEndpoints = nextInt()
            nRequestDescriptions = nextInt()
            nCaches = nextInt()
            cacheSize = nextInt()
            videoSizes = nextLineAsIntArray()
        }
        problem.endpoints = Array(problem.nEndpoints) { readEndpoint() }
        problem.requestDescs = Array(problem.nRequestDescriptions) { readRequest() }
        return problem
    }

    private fun HCReader.readEndpoint(): Endpoint {
        val endpoint = Endpoint().apply {
            dcLatency = nextInt()
        }
        val K = nextInt()
        val latencies = Array(K) { readLatency() }
        endpoint.setLatencies(latencies)
        return endpoint
    }

    private fun HCReader.readLatency(): Latency = Latency().apply {
        cacheId = nextInt()
        latency = nextInt()
    }

    private fun HCReader.readRequest(): RequestDesc = RequestDesc().apply {
        videoId = nextInt()
        endpointId = nextInt()
        count = nextInt()
    }

    @Test
    fun test_parser() {
        val problem = readHashCodeInput(input) { readStreamingProblem() }

        assertEquals(5, problem.nVideos.toLong())
        assertEquals(2, problem.nEndpoints.toLong())
        assertEquals(4, problem.nRequestDescriptions.toLong())
        assertEquals(3, problem.nCaches.toLong())
        assertEquals(100, problem.cacheSize.toLong())
        assertArrayEquals(intArrayOf(50, 50, 80, 30, 110), problem.videoSizes)

        assertEquals(2, problem.endpoints.size.toLong())

        assertEquals(1000, problem.endpoints[0].dcLatency.toLong())
        assertEquals(3, problem.endpoints[0].cacheLatencies.size.toLong())
        assertEquals(Integer.valueOf(100), problem.endpoints[0].cacheLatencies.get(0))
        assertEquals(Integer.valueOf(200), problem.endpoints[0].cacheLatencies.get(2))
        assertEquals(Integer.valueOf(300), problem.endpoints[0].cacheLatencies.get(1))

        assertEquals(500, problem.endpoints[1].dcLatency.toLong())
        assertEquals(0, problem.endpoints[1].cacheLatencies.size.toLong())

        assertEquals(4, problem.requestDescs.size.toLong())

        assertEquals(1500, problem.requestDescs[0].count.toLong())
        assertEquals(3, problem.requestDescs[0].videoId.toLong())
        assertEquals(0, problem.requestDescs[0].endpointId.toLong())

        assertEquals(1000, problem.requestDescs[1].count.toLong())
        assertEquals(0, problem.requestDescs[1].videoId.toLong())
        assertEquals(1, problem.requestDescs[1].endpointId.toLong())

        assertEquals(500, problem.requestDescs[2].count.toLong())
        assertEquals(4, problem.requestDescs[2].videoId.toLong())
        assertEquals(0, problem.requestDescs[2].endpointId.toLong())

        assertEquals(1000, problem.requestDescs[3].count.toLong())
        assertEquals(1, problem.requestDescs[3].videoId.toLong())
        assertEquals(0, problem.requestDescs[3].endpointId.toLong())
    }
}
