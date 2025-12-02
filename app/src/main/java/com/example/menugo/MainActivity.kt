package com.example.menugo

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.menugo.Entity.Product
import com.example.menugo.controller.ProductController
import com.example.menugo.data.IDataManager
import com.example.menugo.data.MemoryDataManager
import com.example.menugo.data.Category
import com.example.menugo.Ui.CategoryAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.example.menugo.util.Util
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestoreSettings
class MainActivity : AppCompatActivity() {

    private lateinit var controller: ProductController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(false)
            .build()
        FirebaseFirestore.getInstance().firestoreSettings = settings

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- CRUD en memoria ---
        val dataManager: IDataManager<Product> = MemoryDataManager()
        controller = ProductController(dataManager)

        controller.addProduct(
            Product(
                id = 1,
                name = "Hamburger Clásica",
                description = "Carne, queso y lechuga",
                price = 2500.0,
                category = "Hamburguesas"
            )
        )

        controller.addProduct(
            Product(
                id = 2,
                name = "Papas Fritas",
                description = "Porción mediana",
                price = 1200.0,
                category = "Acompañamientos"
            )
        )

        val lista = controller.getAllProducts()
        Util.showToast(this, "Productos cargados: ${lista.size}")

        // --- RecyclerView de categorías ---
        val rvCategorie = findViewById<RecyclerView>(R.id.rvCategories)

        val categories = listOf(
            Category(1, "Hamburguesas", android.R.drawable.ic_menu_gallery),
            Category(2, "Bebidas", android.R.drawable.ic_menu_gallery),
            Category(3, "Postres", android.R.drawable.ic_menu_gallery),
            Category(4, "Acompañamientos", android.R.drawable.ic_menu_gallery),
            Category(5, "Combos", android.R.drawable.ic_menu_gallery),
            Category(6, "Promociones", android.R.drawable.ic_menu_gallery)
        )

        rvCategorie.layoutManager = GridLayoutManager(this, 3)
        rvCategorie.adapter = CategoryAdapter(categories) { category ->
            val intent = Intent(this, ProductListActivity::class.java)
            intent.putExtra("category", category.name)
            startActivity(intent)
        }

        testFirebase()

    }

    private fun testFirebase() {
        val db = FirebaseFirestore.getInstance()
        db.collection("products")
            .get()
            .addOnSuccessListener { snapshot ->
                val msg = "Firebase OK: ${snapshot.size()} docs"
                Log.d("FirebaseTest", msg)
                Util.showToast(this, msg)
            }
            .addOnFailureListener { e ->
                val msg = "Firebase error: ${e.message}"
                Log.e("FirebaseTest", msg, e)
                Util.showToast(this, msg)
            }
    }



}
