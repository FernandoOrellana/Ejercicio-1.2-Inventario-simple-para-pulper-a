package com.uth.ejercicio12inventariosimpleparapulpera.ui.products

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.uth.ejercicio12inventariosimpleparapulpera.R
import com.uth.ejercicio12inventariosimpleparapulpera.data.model.Product
import com.uth.ejercicio12inventariosimpleparapulpera.databinding.ItemProductBinding
import com.uth.ejercicio12inventariosimpleparapulpera.util.CurrencyFormatter

class ProductAdapter(
    private var products: List<Product> = emptyList(),
    private val onEditClick: (Product) -> Unit,
    private val onDeleteClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    fun submitList(newList: List<Product>) {
        products = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size

    inner class ProductViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            val context = binding.root.context

            binding.tvProductName.text = product.name
            binding.tvPrice.text = "Precio: ${CurrencyFormatter.format(product.price)}"
            binding.tvTotalValue.text = "Valor: ${CurrencyFormatter.format(product.totalValue)}"

            if (!product.barcode.isNull_or_blank()) {
                binding.tvBarcode.visibility = View.VISIBLE
                binding.tvBarcode.text = "Cód: ${product.barcode}"
            } else {
                binding.tvBarcode.visibility = View.GONE
            }

            if (product.isLowStock) {
                binding.tvStockBadge.text = "¡STOCK BAJO! ${product.stock} / Mín: ${product.minStock}"
                binding.tvStockBadge.setTextColor(ContextCompat.getColor(context, R.color.stock_low))
                binding.tvStockBadge.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.stock_low_bg)
                )
            } else {
                binding.tvStockBadge.text = "Stock: ${product.stock} (Mín: ${product.minStock})"
                binding.tvStockBadge.setTextColor(ContextCompat.getColor(context, R.color.stock_normal))
                binding.tvStockBadge.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.stock_normal_bg)
                )
            }

            binding.btnEdit.setOnClickListener { onEditClick(product) }
            binding.btnDelete.setOnClickListener { onDeleteClick(product) }
        }
    }

    private fun String?.isNull_or_blank(): Boolean {
        return this == null || this.trim().isEmpty()
    }
}
