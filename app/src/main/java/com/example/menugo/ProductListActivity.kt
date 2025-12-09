package com.example.menugo

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.menugo.AddProductActivity
import com.example.menugo.CartActivity
import com.example.menugo.ProductDetailActivity
import com.example.menugo.Entity.Product
import com.example.menugo.R
import com.example.menugo.Ui.ProductAdapter
import com.example.menugo.data.ProductStore
import com.example.menugo.Util.UserRole
import com.example.menugo.Util.Util
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class ProductListActivity : AppCompatActivity() {

    private lateinit var rvProducts: RecyclerView
    private lateinit var txtCategoryTitle: TextView
    private lateinit var etSearchProduct: EditText
    private lateinit var fabAddProduct: FloatingActionButton
    private lateinit var fabOpenCart: FloatingActionButton
    private lateinit var adapter: ProductAdapter

    private lateinit var categoryName: String
    private var allProducts: List<Product> = emptyList()
    private val docIdsByProductId = mutableMapOf<Int, String>()

    private var db: FirebaseFirestore? = null

    // Rol que viene desde MainActivity (por defecto cliente)
    private var userRole: String = UserRole.ROLE_CLIENT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_product_list)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainProductList)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --------- Datos que nos llegan ---------
        categoryName = intent.getStringExtra("category") ?: "Productos"

        val rawRole = intent.getStringExtra(UserRole.EXTRA_USER_ROLE) ?: UserRole.ROLE_CLIENT
        userRole = rawRole.trim().lowercase()

        // (solo para depurar, puedes quitar estos toasts)
        // Util.showToast(this, if (userRole == UserRole.ROLE_ADMIN) "Modo administrador" else "Modo cliente")

        // --------- Referencias a vistas ---------
        rvProducts = findViewById(R.id.rvProducts)
        txtCategoryTitle = findViewById(R.id.txtCategoryTitle)
        etSearchProduct = findViewById(R.id.etSearchProduct)
        fabAddProduct = findViewById(R.id.fabAddProduct)
        fabOpenCart = findViewById(R.id.fabOpenCart)

        txtCategoryTitle.text = categoryName

        // --------- Firebase ---------
        try {
            FirebaseApp.initializeApp(this)
            db = FirebaseFirestore.getInstance()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            db = null
        }

        // --------- Mostrar / ocultar FAB según rol ---------
        if (userRole == UserRole.ROLE_ADMIN) {
            fabAddProduct.show()
            fabAddProduct.setOnClickListener {
                val intent = Intent(this, AddProductActivity::class.java).apply {
                    putExtra("category", categoryName)
                }
                startActivity(intent)
            }
        } else {
            fabAddProduct.hide()
        }
        // --------- Cargar productos ---------
        loadProducts()

        // --------- Adapter ---------
        adapter = ProductAdapter(
            products = allProducts,
            onItemClick = { product ->
                // Ir al detalle, pasamos todos los datos
                val intent = Intent(this, ProductDetailActivity::class.java).apply {
                    putExtra("id", product.id)
                    putExtra("name", product.name)
                    putExtra("description", product.description)
                    putExtra("price", product.price)
                    putExtra("category", product.category)
                    putExtra("imageUri", product.imageUri)
                    putExtra(UserRole.EXTRA_USER_ROLE, userRole)
                }
                startActivity(intent)
            },
            onItemLongClick = { product ->
                // Sólo el admin puede eliminar
                if (userRole == UserRole.ROLE_ADMIN) {
                    showDeleteDialog(product)
                } else {
                    Util.showToast(this, "Solo el administrador puede eliminar productos")
                }
            }
        )

        rvProducts.layoutManager = LinearLayoutManager(this)
        rvProducts.adapter = adapter

        // --------- Búsqueda ---------
        setupSearch()

        // --------- Botones flotantes ---------
        fabAddProduct.setOnClickListener {
            if (userRole == UserRole.ROLE_ADMIN) {
                val intent = Intent(this, AddProductActivity::class.java).apply {
                    putExtra("category", categoryName)
                    putExtra(UserRole.EXTRA_USER_ROLE, userRole)
                }
                startActivity(intent)
            } else {
                Util.showToast(this, "Solo el administrador puede agregar productos")
            }
        }

        fabOpenCart.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadProducts()
    }

    // --------- Cargar productos desde Firestore o local ---------
    private fun loadProducts() {
        val firestore = db
        if (firestore == null) {
            // Modo local si Firebase falla
            allProducts = ProductStore.products.filter {
                it.category.equals(categoryName, ignoreCase = true)
            }
            applyFilter(etSearchProduct.text.toString())
            return
        }

        firestore.collection("products")
            .get()
            .addOnSuccessListener { snapshot ->
                docIdsByProductId.clear()

                val mapped = snapshot.documents.map { doc ->
                    val data = doc.data ?: emptyMap<String, Any?>()

                    val id = (data["id"] as? Number)?.toInt() ?: 0
                    val name = (data["name"] ?: data["name "] ?: "Sin nombre").toString()
                    val description = (data["description"] ?: data["descripcion"] ?: "").toString()
                    val price = (data["price"] as? Number)?.toDouble() ?: 0.0
                    val category = (data["category"] ?: data["categoria"] ?: "").toString()
                    val imageUri = (data["imageUri"] ?: "").toString()

                    if (id != 0) {
                        docIdsByProductId[id] = doc.id
                    }

                    Product(id, name, description, price, category, imageUri)
                }

                allProducts = mapped.filter { product ->
                    product.category.trim()
                        .equals(categoryName.trim(), ignoreCase = true)
                }

                applyFilter(etSearchProduct.text.toString())
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                allProducts = ProductStore.products.filter {
                    it.category.equals(categoryName, ignoreCase = true)
                }
                applyFilter(etSearchProduct.text.toString())
            }
    }

    // --------- Búsqueda local en la lista ---------
    private fun setupSearch() {
        etSearchProduct.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilter(s.toString())
            }
        })
    }

    private fun applyFilter(query: String) {
        val filtered = if (query.isBlank()) {
            allProducts
        } else {
            allProducts.filter { it.name.contains(query, ignoreCase = true) }
        }
        if (::adapter.isInitialized) {
            adapter.updateData(filtered)
        }
    }

    // --------- Eliminar producto ---------
    private fun showDeleteDialog(product: Product) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar producto")
            .setMessage("¿Desea eliminar '${product.name}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                val firestore = db
                val docId = docIdsByProductId[product.id]

                if (firestore != null && docId != null) {
                    firestore.collection("products").document(docId)
                        .delete()
                        .addOnSuccessListener {
                            Util.showToast(this, "Producto eliminado")
                            loadProducts()
                        }
                        .addOnFailureListener { e ->
                            Util.showToast(this, "Error al eliminar: ${e.message}")
                        }
                } else {
                    // Fallback local
                    ProductStore.products.removeAll { it.id == product.id }
                    loadProducts()
                    Util.showToast(this, "Producto eliminado (local)")
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
