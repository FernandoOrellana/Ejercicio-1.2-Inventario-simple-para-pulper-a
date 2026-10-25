package com.uth.ejercicio12inventariosimpleparapulpera.ui.products

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.uth.ejercicio12inventariosimpleparapulpera.R
import com.uth.ejercicio12inventariosimpleparapulpera.data.model.Product
import com.uth.ejercicio12inventariosimpleparapulpera.data.repository.InventoryRepository
import com.uth.ejercicio12inventariosimpleparapulpera.databinding.FragmentProductsBinding

class ProductsFragment : Fragment() {

    private var _binding: FragmentProductsBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: InventoryRepository
    private lateinit var adapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = InventoryRepository(requireContext())

        setupRecyclerView()
        setupSearch()

        binding.fabAddProduct.setOnClickListener {
            openProductDialog()
        }

        loadProducts()
    }

    override fun onResume() {
        super.onResume()
        loadProducts()
    }

    private fun setupRecyclerView() {
        adapter = ProductAdapter(
            onEditClick = { product ->
                openProductDialog(product.id)
            },
            onDeleteClick = { product ->
                confirmDeleteProduct(product)
            }
        )
        binding.rvProducts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvProducts.adapter = adapter
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                loadProducts(s?.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    fun loadProducts(query: String? = binding.etSearch.text?.toString()) {
        val products = repository.getAllProducts(query)
        adapter.submitList(products)

        if (products.isEmpty()) {
            binding.layoutEmptyProducts.visibility = View.VISIBLE
            binding.rvProducts.visibility = View.GONE
        } else {
            binding.layoutEmptyProducts.visibility = View.GONE
            binding.rvProducts.visibility = View.VISIBLE
        }
    }

    private fun openProductDialog(productId: Long = -1L) {
        val dialog = ProductDialogFragment.newInstance(productId)
        dialog.setOnProductSavedListener {
            loadProducts()
        }
        dialog.show(childFragmentManager, "ProductDialogFragment")
    }

    private fun confirmDeleteProduct(product: Product) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.confirm_delete_title)
            .setMessage(getString(R.string.confirm_delete_msg, product.name))
            .setPositiveButton(R.string.btn_delete) { _, _ ->
                val deleted = repository.deleteProduct(product.id)
                if (deleted) {
                    Toast.makeText(requireContext(), "Producto eliminado", Toast.LENGTH_SHORT).show()
                    loadProducts()
                } else {
                    Toast.makeText(requireContext(), "Error al eliminar producto", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
