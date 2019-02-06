package org.hildan.hashcode.utils.examples.drones

class Simulation(
    val nRows: Int,
    val nCols: Int,
    val nDrones: Int,
    val nTurns: Int,
    val maxLoad: Int,
    val nProductTypes: Int,
    val productTypeWeights: IntArray,
    val warehouses: Array<Warehouse>,
    val orders: Array<Order>
)

class Warehouse(
    val row: Int,
    val col: Int,
    val stocks: IntArray
)

class Order(
    val id: Int,
    val row: Int,
    val col: Int,
    nProductTypes: Int,
    products: IntArray
) {
    val quantities: IntArray = IntArray(nProductTypes).apply {
        for (p in products) {
            this[p]++
        }
    }
}
