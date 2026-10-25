package com.uth.ejercicio12inventariosimpleparapulpera

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.uth.ejercicio12inventariosimpleparapulpera.data.repository.InventoryRepository
import com.uth.ejercicio12inventariosimpleparapulpera.databinding.ActivityMainBinding
import com.uth.ejercicio12inventariosimpleparapulpera.ui.movements.MovementsFragment
import com.uth.ejercicio12inventariosimpleparapulpera.ui.products.ProductsFragment
import com.uth.ejercicio12inventariosimpleparapulpera.ui.summary.SummaryFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        // Initialize sample data if database is empty
        val repository = InventoryRepository(this)
        repository.seedSampleDataIfEmpty()

        setupBottomNavigation()

        if (savedInstanceState == null) {
            loadFragment(ProductsFragment())
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_products -> {
                    loadFragment(ProductsFragment())
                    true
                }
                R.id.nav_movements -> {
                    loadFragment(MovementsFragment())
                    true
                }
                R.id.nav_summary -> {
                    loadFragment(SummaryFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
