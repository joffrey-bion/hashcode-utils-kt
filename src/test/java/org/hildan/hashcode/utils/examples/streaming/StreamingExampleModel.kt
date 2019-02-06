package org.hildan.hashcode.utils.examples.streaming

import java.util.ArrayList

class StreamingProblem(
    var nVideos: Int,
    var nEndpoints: Int,
    var nRequestDescriptions: Int,
    var nCaches: Int,
    var cacheSize: Int,
    var videoSizes: IntArray,
    var endpoints: Array<Endpoint>,
    var requestDescs: Array<RequestDesc>
) {
    fun solve(): List<String> {
        return ArrayList()
    }
}

class Endpoint(
    val dcLatency: Int,
    latencies: Array<Latency>
) {
    val cacheIds: IntArray = latencies.map { l -> l.cacheId }.toIntArray()
    val cacheLatencies: MutableMap<Int, Int> = latencies.map { it.cacheId to it.latency }.toMap(HashMap())
    val gainPerCache: MutableMap<Int, Int> = latencies.map { it.cacheId to dcLatency - it.latency }.toMap(HashMap())
    val nRequestsPerVideo: MutableMap<Video, Long> = HashMap()

    fun addRequests(video: Video, nbRequests: Int) {
        nRequestsPerVideo.putIfAbsent(video, 0L)
        nRequestsPerVideo.compute(video) { _, `val` -> `val`!! + nbRequests }
    }

    fun getNbRequests(video: Video): Long = nRequestsPerVideo.getOrDefault(video, 0L)
}

class Latency(val cacheId: Int, val latency: Int)

class RequestDesc(var count: Int, var videoId: Int, var endpointId: Int)

class Video
