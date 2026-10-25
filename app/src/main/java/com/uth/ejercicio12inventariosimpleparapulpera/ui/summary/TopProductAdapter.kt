package com.uth.ejercicio12inventariosimpleparapulpera.ui.summary

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.uth.ejercicio12inventariosimpleparapulpera.R
import com.uth.ejercicio12inventariosimpleparapulpera.data.model.Product
import com.uth.ejercicio12inventariosimpleparapulpera.databinding.ItemTopProductBinding
import com.uth.ejercicio12inventariosimpleparapulpera.util.CurrencyFormatter

class TopProductAdapter(
    private var products: List<Product> = emptyList()
) : RecyclerView.Adapter<TopProductAdapter.TopProductViewHolder>() {

    fun submitList(newList: List<Product>) {
        products = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopProductViewHolder {
        val binding = ItemTopProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TopProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TopProductViewHolder, position: Int) {
        holder.bind(products[position], position + 1)
    }

    override fun getItemCount(): Int = products.size

    inner class TopProductViewHolder(private val binding: ItemTopProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product, rank: Int) {
            val context = binding.root.context
            binding.tvRank.text = "#$rank"

            if (rank == 1) {
                binding.tvRank.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.rank_gold)
                )
                binding.tvRank.setTextColor(ContextCompat.getColor(context, R.color.black))
            } else {
                binding.tvRank.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.primary_light)
                )
                binding.tvRank.setTextColor(ContextCompat.getColor(context, R.color.primary_dark))
            }

            binding.tvProductName.text = product.name
            binding.tvDetails.text = "Precio: ${CurrencyFormatter.format(product.price)} | Stock: ${product.stock}"
            binding.tvTotalValue.text = CurrencyFormatter.format(product.totalValue)
        }
    }
}
