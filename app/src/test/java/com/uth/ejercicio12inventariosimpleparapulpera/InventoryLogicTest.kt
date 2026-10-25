package com.uth.ejercicio12inventariosimpleparapulpera

import com.uth.ejercicio12inventariosimpleparapulpera.data.model.InventorySummary
import com.uth.ejercicio12inventariosimpleparapulpera.data.model.Product
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InventoryLogicTest {

    @Test
    fun product_totalValueCalculation_isCorrect() {
        val product = Product(
            id = 1,
            name = "Coca Cola 500ml",
            price = 22.00,
            stock = 10,
            barcode = "7501055300012",
            minStock = 5
        )

        assertEquals(220.00, product.totalValue, 0.001)
    }

    @Test
    fun product_lowStockThreshold_isCorrect() {
        val normalStockProduct = Product(name = "Arroz", price = 15.0, stock = 10, minStock = 5)
        val lowStockProduct = Product(name = "Frijoles", price = 20.0, stock = 5, minStock = 5)
        val criticalStockProduct = Product(name = "Aceite", price = 45.0, stock = 2, minStock = 5)

        assertFalse(normalStockProduct.isLowStock)
        assertTrue(lowStockProduct.isLowStock)
        assertTrue(criticalStockProduct.isLowStock)
    }

    @Test
    fun inventorySummary_calculation_isCorrect() {
        val p1 = Product(id = 1, name = "P1", price = 10.0, stock = 5, minStock = 3) // Value: 50
        val p2 = Product(id = 2, name = "P2", price = 20.0, stock = 2, minStock = 5) // Value: 40, Low stock
        val p3 = Product(id = 3, name = "P3", price = 100.0, stock = 1, minStock = 2) // Value: 100, Low stock

        val products = listOf(p1, p2, p3)
        val totalVal = products.sumOf { it.totalValue }
        val lowStockList = products.filter { it.isLowStock }
        val top5 = products.sortedByDescending { it.totalValue }.take(5)

        val summary = InventorySummary(
            totalValue = totalVal,
            totalProducts = products.size,
            lowStockCount = lowStockList.size,
            top5Products = top5,
            lowStockProducts = lowStockList
        )

        assertEquals(190.0, summary.totalValue, 0.001)
        assertEquals(3, summary.totalProducts)
        assertEquals(2, summary.lowStockCount)
        assertEquals(3, summary.top5Products.size)
        assertEquals("P3", summary.top5Products[0].name) // Top #1 should be P3 (100)
    }
}
