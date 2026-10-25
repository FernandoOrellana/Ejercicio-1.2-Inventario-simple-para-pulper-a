package com.uth.ejercicio12inventariosimpleparapulpera.data.model

data class Product(
    val id: Long = 0,
    val name: String,
    val price: Double,
    val stock: Int,
    val barcode: String? = null,
    val minStock: Int = 5
) {
    val totalValue: Double
        get() = price * stock

    val isLowStock: Boolean
        get() = stock <= minStock
}
