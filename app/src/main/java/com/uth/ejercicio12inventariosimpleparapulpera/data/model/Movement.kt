package com.uth.ejercicio12inventariosimpleparapulpera.data.model

data class Movement(
    val id: Long = 0,
    val productId: Long,
    val productName: String = "",
    val qty: Int,
    val type: MovementType,
    val date: String
)
