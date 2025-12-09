package com.example.menugo

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.menugo.Entity.Product
import com.example.menugo.Ui.CategoryAdapter
import com.example.menugo.ProductListActivity
import com.example.menugo.controller.ProductController
import com.example.menugo.data.Category
import com.example.menugo.data.IDataManager
import com.example.menugo.data.MemoryDataManager
import com.example.menugo.Util.UserRole
import com.example.menugo.Util.Util
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var controller: ProductController
    private lateinit var auth: FirebaseAuth

    // Rol que viene desde LoginActivity (por defecto cliente)
    private var userRole: String = UserRole.ROLE_CLIENT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Toolbar como ActionBar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Recibimos el rol desde LoginActivity (si viene)
        userRole = intent.getStringExtra(UserRole.EXTRA_USER_ROLE) ?: UserRole.ROLE_CLIENT

        // Inicializar Firebase
        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- CRUD en memoria (solo demo) ---
        val dataManager: IDataManager<Product> = MemoryDataManager()
        controller = ProductController(dataManager)

        controller.addProduct(
            Product(
                id = 1,
                name = "Hamburguesa Clásica",
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

        // --- RecyclerView de categorías ---
        val rvCategories = findViewById<RecyclerView>(R.id.rvCategories)

        val categories = listOf(
            Category(1, "Hamburguesas", android.R.drawable.ic_menu_gallery),
            Category(2, "Bebidas", android.R.drawable.ic_menu_gallery),
            Category(3, "Postres", android.R.drawable.ic_menu_gallery),
            Category(4, "Acompañamientos", android.R.drawable.ic_menu_gallery),
            Category(5, "Combos", android.R.drawable.ic_menu_gallery),
            Category(6, "Promociones", android.R.drawable.ic_menu_gallery)
        )

        rvCategories.layoutManager = GridLayoutManager(this, 3)
        rvCategories.adapter = CategoryAdapter(categories) { category ->
            val intent = Intent(this, ProductListActivity::class.java).apply {
                putExtra("category", category.name)
                putExtra(UserRole.EXTRA_USER_ROLE, userRole) // pasamos el rol
            }
            startActivity(intent)
        }
    }

    // ---------- MENÚ SUPERIOR (Buscar + Cerrar sesión) ----------

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
                // Comportamiento sencillo para que no parezca que no hace nada
                Util.showToast(
                    this,
                    "Para buscar, entra en una categoría y usa la barra de búsqueda."
                )
                true
            }
            R.id.action_logout -> {
                // Cerrar sesión y volver al login
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
