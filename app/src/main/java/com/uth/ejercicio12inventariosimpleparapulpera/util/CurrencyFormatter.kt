package com.uth.ejercicio12inventariosimpleparapulpera.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    fun format(amount: Double): String {
        val numberFormat = NumberFormat.getCurrencyInstance(Locale("es", "HN"))
        val formatted = numberFormat.format(amount)
        // If Locale doesn't format with L., fallback or clean formatting
        return if (formatted.contains("HNL") || formatted.contains("L")) {
            formatted
        } else {
            String.format(Locale.US, "L. %.2f", amount)
        }
    }
}
