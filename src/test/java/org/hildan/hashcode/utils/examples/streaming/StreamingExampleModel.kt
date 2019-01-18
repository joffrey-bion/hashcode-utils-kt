package org.hildan.hashcode.utils.examples.streaming

import java.util.ArrayList
import java.util.Arrays
import java.util.HashMap

class StreamingProblem {
    var nVideos: Int = 0
    var nEndpoints: Int = 0
    var nRequestDescriptions: Int = 0
    var nCaches: Int = 0
    var cacheSize: Int = 0
    var videoSizes: IntArray = IntArray(0)
    var endpoints: Array<Endpoint> = emptyArray()
    var requestDescs: Array<RequestDesc> = emptyArray()

    fun solve(): List<String> {
        return ArrayList()
    }
}

class Endpoint {
    var dcLatency: Int = 0
    var cacheIds: IntArray = IntArray(0)
    var cacheLatencies: MutableMap<Int, Int> = HashMap()
    var gainPerCache: MutableMap<Int, Int> = HashMap()
    var nRequestsPerVideo: MutableMap<Video, Long> = HashMap()

    fun setLatencies(latencies: Array<Latency>) {
        cacheIds = Arrays.stream(latencies).mapToInt { l -> l.cacheId }.toArray()
        Arrays.stream(latencies).forEach { l -> cacheLatencies[l.cacheId] = l.latency }
        Arrays.stream(latencies).forEach { l -> gainPerCache[l.cacheId] = dcLatency - l.latency }
    }

    fun addRequests(video: Video, nbRequests: Int) {
        nRequestsPerVideo.putIfAbsent(video, 0L)
        nRequestsPerVideo.compute(video) { _, `val` -> `val`!! + nbRequests }
    }

    fun getNbRequests(video: Video): Long = nRequestsPerVideo.getOrDefault(video, 0L)
}

class Latency {
    var cacheId: Int = 0
    var latency: Int = 0
}

class RequestDesc {
    var count: Int = 0
    var videoId: Int = 0
    var endpointId: Int = 0
}

class Video
