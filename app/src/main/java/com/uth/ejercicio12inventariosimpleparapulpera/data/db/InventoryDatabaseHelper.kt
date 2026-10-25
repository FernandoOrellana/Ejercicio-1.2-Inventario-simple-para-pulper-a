package com.uth.ejercicio12inventariosimpleparapulpera.data.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class InventoryDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "pulperia_inventario.db"
        private const val DATABASE_VERSION = 1

        // Table Products
        const val TABLE_PRODUCTS = "products"
        const val COL_PROD_ID = "id"
        const val COL_PROD_NAME = "name"
        const val COL_PROD_PRICE = "price"
        const val COL_PROD_STOCK = "stock"
        const val COL_PROD_BARCODE = "barcode"
        const val COL_PROD_MIN_STOCK = "min_stock"

        // Table Movements
        const val TABLE_MOVEMENTS = "movements"
        const val COL_MOV_ID = "id"
        const val COL_MOV_PRODUCT_ID = "product_id"
        const val COL_MOV_QTY = "qty"
        const val COL_MOV_TYPE = "type"
        const val COL_MOV_DATE = "date"
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createProductsTable = """
            CREATE TABLE $TABLE_PRODUCTS (
                $COL_PROD_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_PROD_NAME TEXT NOT NULL,
                $COL_PROD_PRICE REAL NOT NULL,
                $COL_PROD_STOCK INTEGER NOT NULL,
                $COL_PROD_BARCODE TEXT,
                $COL_PROD_MIN_STOCK INTEGER NOT NULL DEFAULT 5
            );
        """.trimIndent()

        val createMovementsTable = """
            CREATE TABLE $TABLE_MOVEMENTS (
                $COL_MOV_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_MOV_PRODUCT_ID INTEGER NOT NULL,
                $COL_MOV_QTY INTEGER NOT NULL,
                $COL_MOV_TYPE TEXT NOT NULL,
                $COL_MOV_DATE TEXT NOT NULL,
                FOREIGN KEY ($COL_MOV_PRODUCT_ID) REFERENCES $TABLE_PRODUCTS($COL_PROD_ID) ON DELETE CASCADE
            );
        """.trimIndent()

        db.execSQL(createProductsTable)
        db.execSQL(createMovementsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_MOVEMENTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PRODUCTS")
        onCreate(db)
    }
}
