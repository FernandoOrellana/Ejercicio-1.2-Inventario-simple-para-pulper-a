package com.uth.ejercicio12inventariosimpleparapulpera.ui.movements

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.uth.ejercicio12inventariosimpleparapulpera.R
import com.uth.ejercicio12inventariosimpleparapulpera.data.model.Movement
import com.uth.ejercicio12inventariosimpleparapulpera.data.model.MovementType
import com.uth.ejercicio12inventariosimpleparapulpera.databinding.ItemMovementBinding

class MovementAdapter(
    private var movements: List<Movement> = emptyList()
) : RecyclerView.Adapter<MovementAdapter.MovementViewHolder>() {

    fun submitList(newList: List<Movement>) {
        movements = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovementViewHolder {
        val binding = ItemMovementBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MovementViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MovementViewHolder, position: Int) {
        holder.bind(movements[position])
    }

    override fun getItemCount(): Int = movements.size

    inner class MovementViewHolder(private val binding: ItemMovementBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(movement: Movement) {
            val context = binding.root.context

            binding.tvProductName.text = movement.productName
            binding.tvDate.text = movement.date

            if (movement.type == MovementType.ENTRADA) {
                binding.tvQty.text = "+ ${movement.qty}"
                binding.tvQty.setTextColor(ContextCompat.getColor(context, R.color.movement_in))
                binding.ivTypeIcon.setImageResource(R.drawable.ic_arrow_upward)
                binding.ivTypeIcon.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.movement_in)
                )
                binding.ivTypeIcon.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.movement_in_bg)
                )
            } else {
                binding.tvQty.text = "- ${movement.qty}"
                binding.tvQty.setTextColor(ContextCompat.getColor(context, R.color.movement_out))
                binding.ivTypeIcon.setImageResource(R.drawable.ic_arrow_downward)
                binding.ivTypeIcon.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.movement_out)
                )
                binding.ivTypeIcon.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(context, R.color.movement_out_bg)
                )
            }
        }
    }
}
