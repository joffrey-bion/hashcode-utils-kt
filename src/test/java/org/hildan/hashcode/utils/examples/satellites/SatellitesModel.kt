package org.hildan.hashcode.utils.examples.satellites

/**
 * Index of the latitude in an array of coordinates.
 */
const val LATITUDE = 0

/**
 * Index of the longitude in an array of coordinates.
 */
const val LONGITUDE = 1

class Simulation(
    val nTurns: Int,
    val satellites: Array<Satellite>,
    val collections: Array<ImageCollection>
)

class ImageCollection(val value: Int) {
    var locations: Array<Location> = emptyArray()
    var ranges: Array<IntArray> = emptyArray()
}

class Picture(
    val position: IntArray,
    val turnTakenAt: Int,
    val satellite: Int
)

class Location(
    var parentCollection: ImageCollection,
    latitude: Int,
    longitude: Int
) {
    var coords = IntArray(2).apply {
        this[LATITUDE] = latitude
        this[LONGITUDE] = longitude
    }

    var pictureTaken = false
}

class Satellite(
    latitude: Int,
    longitude: Int,
    var latitudeVelocity: Int,
    val maxOrientationChangePerTurn: Int,
    val maxOrientationValue: Int
) {
    var position: IntArray = IntArray(2).apply {
        this[LATITUDE] = latitude
        this[LONGITUDE] = longitude
    }
}
