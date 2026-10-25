package com.uth.ejercicio12inventariosimpleparapulpera.data.repository

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.uth.ejercicio12inventariosimpleparapulpera.data.db.InventoryDatabaseHelper
import com.uth.ejercicio12inventariosimpleparapulpera.data.model.InventorySummary
import com.uth.ejercicio12inventariosimpleparapulpera.data.model.Movement
import com.uth.ejercicio12inventariosimpleparapulpera.data.model.MovementType
import com.uth.ejercicio12inventariosimpleparapulpera.data.model.Product
import com.uth.ejercicio12inventariosimpleparapulpera.util.DateFormatter

sealed class MovementResult {
    data class Success(val newStock: Int, val isLowStock: Boolean, val productName: String) : MovementResult()
    data class InsufficientStock(val currentStock: Int, val requestedQty: Int) : MovementResult()
    data class Error(val message: String) : MovementResult()
}

class InventoryRepository(context: Context) {

    private val dbHelper = InventoryDatabaseHelper(context.applicationContext)

    fun getAllProducts(query: String? = null): List<Product> {
        val products = mutableListOf<Product>()
        val db = dbHelper.readableDatabase

        var selection: String? = null
        var selectionArgs: Array<String>? = null

        if (!query.isNull_or_blank()) {
            val q = "%${query!!.trim()}%"
            selection = "${InventoryDatabaseHelper.COL_PROD_NAME} LIKE ? OR ${InventoryDatabaseHelper.COL_PROD_BARCODE} LIKE ?"
            selectionArgs = arrayOf(q, q)
        }

        val cursor = db.query(
            InventoryDatabaseHelper.TABLE_PRODUCTS,
            null,
            selection,
            selectionArgs,
            null,
            null,
            "${InventoryDatabaseHelper.COL_PROD_NAME} ASC"
        )

        cursor.use {
            while (it.moveToNext()) {
                products.add(cursorToProduct(it))
            }
        }
        return products
    }

    private fun String?.isNull_or_blank(): Boolean {
        return this == null || this.trim().isEmpty()
    }

    fun getProductById(id: Long): Product? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            InventoryDatabaseHelper.TABLE_PRODUCTS,
            null,
            "${InventoryDatabaseHelper.COL_PROD_ID} = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )

        cursor.use {
            if (it.moveToFirst()) {
                return cursorToProduct(it)
            }
        }
        return null
    }

    fun getProductByBarcode(barcode: String): Product? {
        if (barcode.isBlank()) return null
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            InventoryDatabaseHelper.TABLE_PRODUCTS,
            null,
            "${InventoryDatabaseHelper.COL_PROD_BARCODE} = ?",
            arrayOf(barcode.trim()),
            null,
            null,
            null
        )

        cursor.use {
            if (it.moveToFirst()) {
                return cursorToProduct(it)
            }
        }
        return null
    }

    fun insertProduct(product: Product): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(InventoryDatabaseHelper.COL_PROD_NAME, product.name.trim())
            put(InventoryDatabaseHelper.COL_PROD_PRICE, product.price)
            put(InventoryDatabaseHelper.COL_PROD_STOCK, product.stock)
            put(InventoryDatabaseHelper.COL_PROD_BARCODE, product.barcode?.trim()?.ifEmpty { null })
            put(InventoryDatabaseHelper.COL_PROD_MIN_STOCK, product.minStock)
        }
        return db.insert(InventoryDatabaseHelper.TABLE_PRODUCTS, null, values)
    }

    fun updateProduct(product: Product): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(InventoryDatabaseHelper.COL_PROD_NAME, product.name.trim())
            put(InventoryDatabaseHelper.COL_PROD_PRICE, product.price)
            put(InventoryDatabaseHelper.COL_PROD_STOCK, product.stock)
            put(InventoryDatabaseHelper.COL_PROD_BARCODE, product.barcode?.trim()?.ifEmpty { null })
            put(InventoryDatabaseHelper.COL_PROD_MIN_STOCK, product.minStock)
        }
        val rows = db.update(
            InventoryDatabaseHelper.TABLE_PRODUCTS,
            values,
            "${InventoryDatabaseHelper.COL_PROD_ID} = ?",
            arrayOf(product.id.toString())
        )
        return rows > 0
    }

    fun deleteProduct(id: Long): Boolean {
        val db = dbHelper.writableDatabase
        val rows = db.delete(
            InventoryDatabaseHelper.TABLE_PRODUCTS,
            "${InventoryDatabaseHelper.COL_PROD_ID} = ?",
            arrayOf(id.toString())
        )
        return rows > 0
    }

    fun registerMovement(productId: Long, qty: Int, type: MovementType): MovementResult {
        if (qty <= 0) {
            return MovementResult.Error("La cantidad debe ser mayor a cero.")
        }

        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            val product = getProductById(productId)
                ?: return MovementResult.Error("Producto no encontrado.")

            val newStock = when (type) {
                MovementType.ENTRADA -> product.stock + qty
                MovementType.SALIDA -> {
                    if (qty > product.stock) {
                        db.endTransaction()
                        return MovementResult.InsufficientStock(product.stock, qty)
                    }
                    product.stock - qty
                }
            }

            // Update product stock
            val stockValues = ContentValues().apply {
                put(InventoryDatabaseHelper.COL_PROD_STOCK, newStock)
            }
            db.update(
                InventoryDatabaseHelper.TABLE_PRODUCTS,
                stockValues,
                "${InventoryDatabaseHelper.COL_PROD_ID} = ?",
                arrayOf(productId.toString())
            )

            // Insert movement record
            val dateStr = DateFormatter.now()
            val movValues = ContentValues().apply {
                put(InventoryDatabaseHelper.COL_MOV_PRODUCT_ID, productId)
                put(InventoryDatabaseHelper.COL_MOV_QTY, qty)
                put(InventoryDatabaseHelper.COL_MOV_TYPE, type.name)
                put(InventoryDatabaseHelper.COL_MOV_DATE, dateStr)
            }
            db.insert(InventoryDatabaseHelper.TABLE_MOVEMENTS, null, movValues)

            db.setTransactionSuccessful()

            val isLowStock = newStock <= product.minStock
            return MovementResult.Success(newStock, isLowStock, product.name)
        } catch (e: Exception) {
            return MovementResult.Error("Error al registrar movimiento: ${e.localizedMessage}")
        } finally {
            db.endTransaction()
        }
    }

    fun getAllMovements(): List<Movement> {
        val movements = mutableListOf<Movement>()
        val db = dbHelper.readableDatabase

        val sql = """
            SELECT m.${InventoryDatabaseHelper.COL_MOV_ID}, 
                   m.${InventoryDatabaseHelper.COL_MOV_PRODUCT_ID}, 
                   p.${InventoryDatabaseHelper.COL_PROD_NAME}, 
                   m.${InventoryDatabaseHelper.COL_MOV_QTY}, 
                   m.${InventoryDatabaseHelper.COL_MOV_TYPE}, 
                   m.${InventoryDatabaseHelper.COL_MOV_DATE}
            FROM ${InventoryDatabaseHelper.TABLE_MOVEMENTS} m
            LEFT JOIN ${InventoryDatabaseHelper.TABLE_PRODUCTS} p ON m.${InventoryDatabaseHelper.COL_MOV_PRODUCT_ID} = p.${InventoryDatabaseHelper.COL_PROD_ID}
            ORDER BY m.${InventoryDatabaseHelper.COL_MOV_ID} DESC
        """.trimIndent()

        val cursor = db.rawQuery(sql, null)
        cursor.use {
            while (it.moveToNext()) {
                val id = it.getLong(0)
                val prodId = it.getLong(1)
                val prodName = it.getString(2) ?: "Producto Eliminado"
                val qty = it.getInt(3)
                val typeStr = it.getString(4)
                val date = it.getString(5)
                val type = try {
                    MovementType.valueOf(typeStr)
                } catch (e: Exception) {
                    MovementType.ENTRADA
                }

                movements.add(
                    Movement(
                        id = id,
                        productId = prodId,
                        productName = prodName,
                        qty = qty,
                        type = type,
                        date = date
                    )
                )
            }
        }
        return movements
    }

    fun getInventorySummary(): InventorySummary {
        val allProducts = getAllProducts()
        var totalValue = 0.0
        val lowStockProducts = mutableListOf<Product>()

        for (product in allProducts) {
            totalValue += product.totalValue
            if (product.isLowStock) {
                lowStockProducts.add(product)
            }
        }

        val top5Products = allProducts.sortedByDescending { it.totalValue }.take(5)

        return InventorySummary(
            totalValue = totalValue,
            totalProducts = allProducts.size,
            lowStockCount = lowStockProducts.size,
            top5Products = top5Products,
            lowStockProducts = lowStockProducts.sortedBy { it.stock }
        )
    }

    fun seedSampleDataIfEmpty() {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM ${InventoryDatabaseHelper.TABLE_PRODUCTS}", null)
        var count = 0
        cursor.use {
            if (it.moveToFirst()) {
                count = it.getInt(0)
            }
        }

        if (count == 0) {
            val sampleProducts = listOf(
                Product(name = "Coca Cola 500ml", price = 22.00, stock = 24, barcode = "7501055300012", minStock = 10),
                Product(name = "Harina Maseca 1kg", price = 28.50, stock = 15, barcode = "7501000111223", minStock = 5),
                Product(name = "Arroz Blanco 1lb", price = 16.00, stock = 40, barcode = "7401002003004", minStock = 10),
                Product(name = "Aceite Vegetal 1L", price = 45.00, stock = 8, barcode = "7401005006007", minStock = 10),
                Product(name = "Leche Sula 1L", price = 32.00, stock = 4, barcode = "7401008009010", minStock = 8),
                Product(name = "Jabón Axion 250g", price = 18.00, stock = 30, barcode = "7501012013014", minStock = 6),
                Product(name = "Galletas Chiky Pack", price = 12.00, stock = 3, barcode = "7401015016017", minStock = 10),
                Product(name = "Café Instantáneo 100g", price = 55.00, stock = 12, barcode = "7401018019020", minStock = 5)
            )

            for (p in sampleProducts) {
                insertProduct(p)
            }
        }
    }

    private fun cursorToProduct(cursor: Cursor): Product {
        val id = cursor.getLong(cursor.getColumnIndexOrThrow(InventoryDatabaseHelper.COL_PROD_ID))
        val name = cursor.getString(cursor.getColumnIndexOrThrow(InventoryDatabaseHelper.COL_PROD_NAME))
        val price = cursor.getDouble(cursor.getColumnIndexOrThrow(InventoryDatabaseHelper.COL_PROD_PRICE))
        val stock = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryDatabaseHelper.COL_PROD_STOCK))
        val barcode = cursor.getString(cursor.getColumnIndexOrThrow(InventoryDatabaseHelper.COL_PROD_BARCODE))
        val minStock = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryDatabaseHelper.COL_PROD_MIN_STOCK))

        return Product(
            id = id,
            name = name,
            price = price,
            stock = stock,
            barcode = barcode,
            minStock = minStock
        )
    }
}
