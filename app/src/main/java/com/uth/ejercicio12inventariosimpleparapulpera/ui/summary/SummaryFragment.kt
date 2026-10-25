package com.uth.ejercicio12inventariosimpleparapulpera.ui.summary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.uth.ejercicio12inventariosimpleparapulpera.R
import com.uth.ejercicio12inventariosimpleparapulpera.data.repository.InventoryRepository
import com.uth.ejercicio12inventariosimpleparapulpera.databinding.FragmentSummaryBinding
import com.uth.ejercicio12inventariosimpleparapulpera.util.CurrencyFormatter

class SummaryFragment : Fragment() {

    private var _binding: FragmentSummaryBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: InventoryRepository
    private lateinit var topProductAdapter: TopProductAdapter
    private lateinit var lowStockAdapter: LowStockAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSummaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = InventoryRepository(requireContext())

        setupRecyclerViews()
        loadSummaryData()
    }

    override fun onResume() {
        super.onResume()
        loadSummaryData()
    }

    private fun setupRecyclerViews() {
        topProductAdapter = TopProductAdapter()
        binding.rvTopProducts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTopProducts.adapter = topProductAdapter

        lowStockAdapter = LowStockAdapter()
        binding.rvLowStock.layoutManager = LinearLayoutManager(requireContext())
        binding.rvLowStock.adapter = lowStockAdapter
    }

    private fun loadSummaryData() {
        val summary = repository.getInventorySummary()

        binding.tvTotalInventoryValue.text = CurrencyFormatter.format(summary.totalValue)
        binding.tvTotalProducts.text = getString(R.string.label_total_products, summary.totalProducts)
        binding.tvLowStockCount.text = getString(R.string.label_low_stock_count, summary.lowStockCount)

        topProductAdapter.submitList(summary.top5Products)

        if (summary.lowStockProducts.isEmpty()) {
            binding.rvLowStock.visibility = View.GONE
            binding.tvNoLowStock.visibility = View.VISIBLE
        } else {
            binding.rvLowStock.visibility = View.VISIBLE
            binding.tvNoLowStock.visibility = View.GONE
            lowStockAdapter.submitList(summary.lowStockProducts)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
