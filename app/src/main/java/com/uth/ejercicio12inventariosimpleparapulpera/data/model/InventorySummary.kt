package com.uth.ejercicio12inventariosimpleparapulpera.data.model

data class InventorySummary(
    val totalValue: Double,
    val totalProducts: Int,
    val lowStockCount: Int,
    val top5Products: List<Product>,
    val lowStockProducts: List<Product>
)
