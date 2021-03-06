package org.hildan.hashcode.utils.examples.satellites

import org.hildan.hashcode.utils.reader.HCReader
import org.hildan.hashcode.utils.reader.withHCReader
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class Satellites {

    private val input = ("3600\n" // Simulation lasts an hour.
            + "2\n" // Two satellites.
            + "170000 8300 300 50 500\n"  // First satellite starts at [170000, 8300], heading north
            + "180000 8300 -300 50 500\n" // Second satellite starts at [180000, 8300], heading south.
            + "3\n"           // Three image collections.
            + "100 1 1\n"     // First image collection is worth 100 points
            + "175958 8387\n" // The only location: Google office in Paris
            + "0 3599\n"      // The image can be taken at any time.
            + "100 1 2\n"     // Second image collection is worth 100 points.
            + "175889 8260\n" // The only location: the Eiffel Tower
            + "0 900\n"       // The image has to be taken in the first 15 minutes...
            + "2700 3599\n"   // ...or in the last 15 minutes
            + "300 2 1\n"     // Third image collection, worth 300 points.
            + "175958 8387\n" // Google office.
            + "175889 8260\n" // The Eiffel Tower.
            + "3300 3599\n")  // The images need to be taken in the last 5 minutes.

    private fun HCReader.readSimulation(): Simulation {
        val nTurns = readInt()
        val S = readInt()
        val satellites = Array(S) { readSatellite() }
        val C =  readInt()
        val collections = Array(C) { readImageCollection() }
        return Simulation(nTurns, satellites, collections)
    }

    private fun HCReader.readSatellite(): Satellite {
        val latitude = readInt()
        val longitude = readInt()
        val v0 = readInt()
        val maxOrientationChange = readInt()
        val maxOrientationValue = readInt()
        return Satellite(
            latitude,
            longitude,
            v0,
            maxOrientationChange,
            maxOrientationValue
        )
    }

    private fun HCReader.readImageCollection(): ImageCollection {
        val value = readInt()
        val imageCollection = ImageCollection(value)
        val L = readInt()
        val R = readInt()
        imageCollection.locations = Array(L) { readLocation(imageCollection) }
        imageCollection.ranges = Array(R) { readRange() }
        return imageCollection
    }

    private fun HCReader.readLocation(collection: ImageCollection): Location {
        val latitude = readInt()
        val longitude = readInt()
        return Location(collection, latitude, longitude)
    }

    private fun HCReader.readRange(): IntArray {
        val latitude = readInt()
        val longitude = readInt()
        return intArrayOf(latitude, longitude)
    }

    @Test
    fun test_parser() {
        val problem = withHCReader(input) { readSimulation() }

        assertEquals(3600, problem.nTurns.toLong())
        assertEquals(2, problem.satellites.size.toLong())

        val sat0 = problem.satellites[0]
        assertArrayEquals(intArrayOf(170000, 8300), sat0.position)
        assertEquals(300, sat0.latitudeVelocity.toLong())
        assertEquals(50, sat0.maxOrientationChangePerTurn.toLong())
        assertEquals(500, sat0.maxOrientationValue.toLong())

        val sat1 = problem.satellites[1]
        assertArrayEquals(intArrayOf(180000, 8300), sat1.position)
        assertEquals(-300, sat1.latitudeVelocity.toLong())
        assertEquals(50, sat1.maxOrientationChangePerTurn.toLong())
        assertEquals(500, sat1.maxOrientationValue.toLong())

        assertEquals(3, problem.collections.size.toLong())

        val coll0 = problem.collections[0]
        assertEquals(100, coll0.value.toLong())
        assertEquals(1, coll0.locations.size.toLong())
        assertEquals(1, coll0.ranges.size.toLong())

        val loc00 = coll0.locations[0]
        assertEquals(coll0, loc00.parentCollection)
        assertArrayEquals(intArrayOf(175958, 8387), loc00.coords)

        assertArrayEquals(intArrayOf(0, 3599), coll0.ranges[0])

        val coll1 = problem.collections[1]
        assertEquals(100, coll1.value.toLong())
        assertEquals(1, coll1.locations.size.toLong())
        assertEquals(2, coll1.ranges.size.toLong())

        val loc10 = coll1.locations[0]
        assertEquals(coll1, loc10.parentCollection)
        assertArrayEquals(intArrayOf(175889, 8260), loc10.coords)

        assertArrayEquals(intArrayOf(0, 900), coll1.ranges[0])
        assertArrayEquals(intArrayOf(2700, 3599), coll1.ranges[1])

        val coll2 = problem.collections[2]
        assertEquals(300, coll2.value.toLong())
        assertEquals(2, coll2.locations.size.toLong())
        assertEquals(1, coll2.ranges.size.toLong())

        val loc20 = coll2.locations[0]
        assertEquals(coll2, loc20.parentCollection)
        assertArrayEquals(intArrayOf(175958, 8387), loc20.coords)

        val loc21 = coll2.locations[1]
        assertEquals(coll2, loc21.parentCollection)
        assertArrayEquals(intArrayOf(175889, 8260), loc21.coords)

        assertArrayEquals(intArrayOf(3300, 3599), coll2.ranges[0])
    }
}
