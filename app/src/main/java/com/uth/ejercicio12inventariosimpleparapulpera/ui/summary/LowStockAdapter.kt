package com.uth.ejercicio12inventariosimpleparapulpera.ui.summary

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.uth.ejercicio12inventariosimpleparapulpera.data.model.Product
import com.uth.ejercicio12inventariosimpleparapulpera.databinding.ItemLowStockBinding

class LowStockAdapter(
    private var products: List<Product> = emptyList()
) : RecyclerView.Adapter<LowStockAdapter.LowStockViewHolder>() {

    fun submitList(newList: List<Product>) {
        products = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LowStockViewHolder {
        val binding = ItemLowStockBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LowStockViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LowStockViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size

    inner class LowStockViewHolder(private val binding: ItemLowStockBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.tvProductName.text = product.name
            binding.tvStockInfo.text = "Stock Actual: ${product.stock} unidades (Mínimo: ${product.minStock})"
        }
    }
}
