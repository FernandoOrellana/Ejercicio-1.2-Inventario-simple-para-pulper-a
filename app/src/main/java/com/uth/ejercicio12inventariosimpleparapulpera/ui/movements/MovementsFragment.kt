package com.uth.ejercicio12inventariosimpleparapulpera.ui.movements

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.uth.ejercicio12inventariosimpleparapulpera.R
import com.uth.ejercicio12inventariosimpleparapulpera.data.model.MovementType
import com.uth.ejercicio12inventariosimpleparapulpera.data.model.Product
import com.uth.ejercicio12inventariosimpleparapulpera.data.repository.InventoryRepository
import com.uth.ejercicio12inventariosimpleparapulpera.data.repository.MovementResult
import com.uth.ejercicio12inventariosimpleparapulpera.databinding.FragmentMovementsBinding

class MovementsFragment : Fragment() {

    private var _binding: FragmentMovementsBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: InventoryRepository
    private lateinit var adapter: MovementAdapter

    private var availableProducts: List<Product> = emptyList()
    private var selectedProduct: Product? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMovementsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = InventoryRepository(requireContext())

        setupRecyclerView()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        loadProductsDropdown()
        loadMovements()
    }

    private fun setupRecyclerView() {
        adapter = MovementAdapter()
        binding.rvMovements.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMovements.adapter = adapter
    }

    private fun loadProductsDropdown() {
        availableProducts = repository.getAllProducts()
        val productLabels = availableProducts.map { getProductLabel(it) }

        val dropdownAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            productLabels
        )

        binding.actvProductSelect.setAdapter(dropdownAdapter)
        binding.actvProductSelect.setOnItemClickListener { _, _, position, _ ->
            val selectedLabel = binding.actvProductSelect.adapter.getItem(position) as? String
            if (selectedLabel != null) {
                selectedProduct = availableProducts.find { p -> getProductLabel(p) == selectedLabel }
                binding.tilSelectProduct.error = null
            }
        }
    }

    private fun getProductLabel(product: Product): String {
        return if (!product.barcode.isNullOrBlank()) {
            "${product.name} [${product.barcode}] (Stock: ${product.stock})"
        } else {
            "${product.name} (Stock: ${product.stock})"
        }
    }

    private fun setupListeners() {
        binding.btnRegisterMovement.setOnClickListener {
            registerMovement()
        }
    }

    private fun registerMovement() {
        var product = selectedProduct
        if (product == null) {
            val inputText = binding.actvProductSelect.text.toString().trim()
            if (inputText.isNotEmpty()) {
                product = availableProducts.find { p ->
                    p.barcode.equals(inputText, ignoreCase = true) ||
                    p.name.equals(inputText, ignoreCase = true) ||
                    getProductLabel(p) == inputText
                }
            }
        }

        if (product == null) {
            binding.tilSelectProduct.error = "Por favor seleccione un producto válido"
            return
        } else {
            binding.tilSelectProduct.error = null
        }

        val qtyStr = binding.etQty.text.toString().trim()
        val qty = qtyStr.toIntOrNull()
        if (qty == null || qty <= 0) {
            binding.tilQty.error = "Ingrese una cantidad válida mayor a 0"
            return
        } else {
            binding.tilQty.error = null
        }

        val isEntrada = binding.toggleMovementType.checkedButtonId == R.id.btn_type_entrada
        val movementType = if (isEntrada) MovementType.ENTRADA else MovementType.SALIDA

        val result = repository.registerMovement(product.id, qty, movementType)

        when (result) {
            is MovementResult.Success -> {
                Toast.makeText(
                    requireContext(),
                    "Movimiento registrado exitosamente",
                    Toast.LENGTH_SHORT
                ).show()

                // Reset form
                binding.etQty.text?.clear()
                binding.actvProductSelect.setText("", false)
                selectedProduct = null

                loadProductsDropdown()
                loadMovements()

                // Check for low stock alert on Salida
                if (result.isLowStock) {
                    showLowStockAlert(result.productName, result.newStock, product.minStock)
                }
            }
            is MovementResult.InsufficientStock -> {
                AlertDialog.Builder(requireContext())
                    .setTitle("Stock Insuficiente")
                    .setMessage(getString(R.string.err_insufficient_stock, result.currentStock, result.requestedQty))
                    .setPositiveButton("Aceptar", null)
                    .show()
            }
            is MovementResult.Error -> {
                Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showLowStockAlert(productName: String, currentStock: Int, minStock: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.alert_low_stock_title)
            .setMessage(getString(R.string.alert_low_stock_msg, productName, currentStock, minStock))
            .setIcon(R.drawable.ic_warning)
            .setPositiveButton("Entendido", null)
            .show()
    }

    private fun loadMovements() {
        val movements = repository.getAllMovements()
        adapter.submitList(movements)

        if (movements.isEmpty()) {
            binding.layoutEmptyMovements.visibility = View.VISIBLE
            binding.rvMovements.visibility = View.GONE
        } else {
            binding.layoutEmptyMovements.visibility = View.GONE
            binding.rvMovements.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
