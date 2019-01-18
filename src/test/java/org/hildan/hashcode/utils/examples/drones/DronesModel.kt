package org.hildan.hashcode.utils.examples.drones

import java.util.ArrayList
import java.util.Arrays

class Simulation(
    val nRows: Int, val nCols: Int, val nDrones: Int, val nTurns: Int, val maxLoad: Int, val nProductTypes: Int
) {
    var productTypeWeights: IntArray = IntArray(0)
    var warehouses: Array<Warehouse> = emptyArray()
    var orders: Array<Order> = emptyArray()
        set(orders) {
            for (i in orders.indices) {
                orders[i].id = i
            }
            field = orders
        }
}

class Warehouse(val row: Int, val col: Int) {
    var stocks: IntArray? = null
}

class Order(val row: Int, val col: Int, nProductTypes: Int) {

    var id: Int = 0
    val quantities: IntArray = IntArray(nProductTypes)
    val totalItemCount: Int
        get() = Arrays.stream(quantities).sum()

    fun setItems(products: IntArray) {
        for (p in products) {
            quantities[p]++
        }
    }
}

class Delivery {
    var commands: List<String> = ArrayList()
    var turns: Int = 0
}
