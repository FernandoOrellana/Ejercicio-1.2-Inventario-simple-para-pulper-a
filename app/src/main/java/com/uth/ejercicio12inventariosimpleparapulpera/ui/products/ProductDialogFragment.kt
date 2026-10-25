package com.uth.ejercicio12inventariosimpleparapulpera.ui.products

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.uth.ejercicio12inventariosimpleparapulpera.R
import com.uth.ejercicio12inventariosimpleparapulpera.data.model.Product
import com.uth.ejercicio12inventariosimpleparapulpera.data.repository.InventoryRepository
import com.uth.ejercicio12inventariosimpleparapulpera.databinding.DialogProductBinding

class ProductDialogFragment : DialogFragment() {

    private var _binding: DialogProductBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: InventoryRepository
    private var editingProductId: Long = -1L
    private var onProductSavedListener: (() -> Unit)? = null

    companion object {
        private const val ARG_PRODUCT_ID = "product_id"

        fun newInstance(productId: Long = -1L): ProductDialogFragment {
            val fragment = ProductDialogFragment()
            val args = Bundle().apply {
                putLong(ARG_PRODUCT_ID, productId)
            }
            fragment.arguments = args
            return fragment
        }
    }

    fun setOnProductSavedListener(listener: () -> Unit) {
        onProductSavedListener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = InventoryRepository(requireContext())
        arguments?.let {
            editingProductId = it.getLong(ARG_PRODUCT_ID, -1L)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (editingProductId != -1L) {
            binding.tvDialogTitle.text = getString(R.string.edit_product)
            loadProductData(editingProductId)
        } else {
            binding.tvDialogTitle.text = getString(R.string.new_product)
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnSave.setOnClickListener {
            saveProduct()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    private fun loadProductData(id: Long) {
        val product = repository.getProductById(id) ?: return
        binding.etProductName.setText(product.name)
        binding.etProductPrice.setText(product.price.toString())
        binding.etProductStock.setText(product.stock.toString())
        binding.etProductBarcode.setText(product.barcode ?: "")
        binding.etProductMinStock.setText(product.minStock.toString())
    }

    private fun saveProduct() {
        val name = binding.etProductName.text.toString().trim()
        val priceStr = binding.etProductPrice.text.toString().trim()
        val stockStr = binding.etProductStock.text.toString().trim()
        val barcodeStr = binding.etProductBarcode.text.toString().trim()
        val minStockStr = binding.etProductMinStock.text.toString().trim()

        if (name.isEmpty()) {
            binding.tilProductName.error = "El nombre es obligatorio"
            return
        } else {
            binding.tilProductName.error = null
        }

        val price = priceStr.toDoubleOrNull()
        if (price == null || price <= 0.0) {
            binding.tilProductPrice.error = "Ingrese un precio válido mayor a cero"
            return
        } else {
            binding.tilProductPrice.error = null
        }

        val stock = stockStr.toIntOrNull()
        if (stock == null || stock < 0) {
            binding.tilProductStock.error = "Ingrese un stock válido (0 o mayor)"
            return
        } else {
            binding.tilProductStock.error = null
        }

        val minStock = minStockStr.toIntOrNull() ?: 5
        if (minStock < 0) {
            binding.tilProductMinStock.error = "El stock mínimo no puede ser negativo"
            return
        } else {
            binding.tilProductMinStock.error = null
        }

        val barcode = barcodeStr.ifEmpty { null }

        // Check if barcode belongs to another product
        if (barcode != null) {
            val existing = repository.getProductByBarcode(barcode)
            if (existing != null && existing.id != editingProductId) {
                binding.tilProductBarcode.error = "Este código de barras ya está asignado a '${existing.name}'"
                return
            }
        }

        val product = Product(
            id = if (editingProductId != -1L) editingProductId else 0L,
            name = name,
            price = price,
            stock = stock,
            barcode = barcode,
            minStock = minStock
        )

        val success = if (editingProductId != -1L) {
            repository.updateProduct(product)
        } else {
            repository.insertProduct(product) > 0
        }

        if (success) {
            Toast.makeText(
                requireContext(),
                if (editingProductId != -1L) "Producto actualizado" else "Producto registrado",
                Toast.LENGTH_SHORT
            ).show()
            onProductSavedListener?.invoke()
            dismiss()
        } else {
            Toast.makeText(requireContext(), "Error al guardar el producto", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
