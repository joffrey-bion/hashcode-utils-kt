package org.hildan.hashcode.utils.examples.drones

import org.hildan.hashcode.utils.reader.HCReader
import org.hildan.hashcode.utils.reader.withHCReader
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class Drones {

    private val input = ("100 100 3 50 500\n"    // 100 rows, 100 columns, 3 drones, 50 turns, max payload is 500u
                    + "3\n"         // There are 3 different product types
                    + "100 5 450\n" // The product types weigh: 100u, 5u, 450u
                    + "2\n"      // There are 2 warehouses
                    + "0 0\n"    // First warehouse is located at [0, 0]
                    + "5 1 0\n"  // It stores 5 items of product 0 and 1 of product 1
                    + "5 5\n"    // Second warehouse is located at [5, 5]
                    + "0 10 2\n" // It stores 10 items of product 1 and 2 items of product 2
                    + "3\n"      // There are 3 orders
                    + "1 1\n"    // First order to be delivered to [1, 1]
                    + "2\n"      // First order contains 2 items
                    + "2 0\n"    // Items of product types: 2, 0
                    + "3 3\n"    // Second order to be delivered to [3, 3]
                    + "1\n"      // Second order contains 1 item
                    + "0\n"      // Items of product types: 0
                    + "5 6\n"    // Third order to be delivered to [5, 6]
                    + "1\n"      // Third order contains 1 item
                    + "2\n")     // Items of product types: 2

    private fun HCReader.readSimulation(): Simulation {
        val nRows = readInt()
        val nCols = readInt()
        val D = readInt()
        val nTurns = readInt()
        val maxLoad = readInt()
        val P = readInt()
        val productTypeWeights = IntArray(P) { readInt() }

        val W = readInt()
        val warehouses = Array(W) { readWarehouse(P) }

        val C = readInt()
        val orders = Array(C) { readOrder(it, P) }

        return Simulation(nRows, nCols, D, nTurns, maxLoad, P, productTypeWeights, warehouses, orders)
    }

    private fun HCReader.readWarehouse(nProductTypes: Int): Warehouse {
        val row = readInt()
        val col = readInt()
        val stocks = IntArray(nProductTypes) { readInt() }
        return Warehouse(row, col, stocks)
    }

    private fun HCReader.readOrder(id: Int, nProductTypes: Int): Order {
        val x = readInt()
        val y = readInt()
        val nProducts = readInt()
        val products = IntArray(nProducts) { readInt() }
        return Order(id, x, y, nProductTypes, products)
    }

    @Test
    fun test() {
        val problem = withHCReader(input) { readSimulation() }

        assertEquals(100, problem.nRows.toLong())
        assertEquals(100, problem.nCols.toLong())
        assertEquals(3, problem.nDrones.toLong())
        assertEquals(50, problem.nTurns.toLong())
        assertEquals(500, problem.maxLoad.toLong())

        assertEquals(3, problem.nProductTypes.toLong())
        assertEquals(3, problem.productTypeWeights.size.toLong())
        assertArrayEquals(intArrayOf(100, 5, 450), problem.productTypeWeights)

        assertEquals(2, problem.warehouses.size.toLong())

        val w0 = problem.warehouses[0]
        assertEquals(0, w0.row.toLong())
        assertEquals(0, w0.col.toLong())
        assertArrayEquals(intArrayOf(5, 1, 0), w0.stocks)

        val w1 = problem.warehouses[1]
        assertEquals(5, w1.row.toLong())
        assertEquals(5, w1.col.toLong())
        assertArrayEquals(intArrayOf(0, 10, 2), w1.stocks)

        assertEquals(3, problem.orders.size.toLong())

        val order0 = problem.orders[0]
        assertEquals(1, order0.row.toLong())
        assertEquals(1, order0.col.toLong())
        assertArrayEquals(intArrayOf(1, 0, 1), order0.quantities)

        val order1 = problem.orders[1]
        assertEquals(3, order1.row.toLong())
        assertEquals(3, order1.col.toLong())
        assertArrayEquals(intArrayOf(1, 0, 0), order1.quantities)

        val order2 = problem.orders[2]
        assertEquals(5, order2.row.toLong())
        assertEquals(6, order2.col.toLong())
        assertArrayEquals(intArrayOf(0, 0, 1), order2.quantities)
    }
}
