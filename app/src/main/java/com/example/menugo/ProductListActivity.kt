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
import com.example.menugo.Entity.Product
import com.example.menugo.Ui.ProductAdapter
import com.example.menugo.util.Util
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source


class ProductListActivity : AppCompatActivity() {

    private lateinit var rvProducts: RecyclerView
    private lateinit var txtCategoryTitle: TextView
    private lateinit var etSearchProduct: EditText
    private lateinit var fabAddProduct: FloatingActionButton
    private lateinit var fabOpenCart: FloatingActionButton
    private lateinit var adapter: ProductAdapter

    private lateinit var categoryName: String
    private var allProducts: List<Product> = emptyList()

    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_product_list)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainProductList)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        rvProducts = findViewById(R.id.rvProducts)
        txtCategoryTitle = findViewById(R.id.txtCategoryTitle)
        etSearchProduct = findViewById(R.id.etSearchProduct)
        fabAddProduct = findViewById(R.id.fabAddProduct)
        fabOpenCart = findViewById(R.id.fabOpenCart)

        categoryName = intent.getStringExtra("category") ?: "Productos"
        txtCategoryTitle.text = categoryName

        adapter = ProductAdapter(
            products = allProducts,
            onItemClick = { product ->
                val i = Intent(this, ProductDetailActivity::class.java)
                i.putExtra("productId", product.id)
                startActivity(i)
            },
            onItemLongClick = { product ->
                showDeleteDialog(product)
            }
        )

        rvProducts.layoutManager = LinearLayoutManager(this)
        rvProducts.adapter = adapter

        setupSearch()

        fabAddProduct.setOnClickListener {
            val i = Intent(this, AddProductActivity::class.java)
            i.putExtra("category", categoryName)
            startActivity(i)
        }

        fabOpenCart.setOnClickListener {
            val i = Intent(this, CartActivity::class.java)
            startActivity(i)
        }

        loadProductsFromFirebase()
    }

    override fun onResume() {
        super.onResume()
        loadProductsFromFirebase()
    }


    private fun loadProductsFromFirebase() {
        db.collection("products")
            .get()
            .addOnSuccessListener { snapshot ->
                val list = mutableListOf<Product>()
                val names = mutableListOf<String>()

                for (doc in snapshot.documents) {
                    val id = (doc.getLong("id") ?: 0L).toInt()
                    val name = doc.getString("name") ?: ""
                    val description = doc.getString("description") ?: ""
                    val price = doc.getDouble("price") ?: 0.0
                    val category = doc.getString("category") ?: ""
                    val imageUri = doc.getString("imageUri")

                    names.add(name)

                    list.add(
                        Product(
                            id = id,
                            name = name,
                            description = description,
                            price = price,
                            category = category,
                            imageUri = imageUri
                        )
                    )
                }

                allProducts = list
                adapter.updateData(allProducts)
                applyFilter(etSearchProduct.text.toString())

                val fromCache = snapshot.metadata.isFromCache
                Util.showToast(
                    this,
                    "Firebase productos: ${list.size} (fromCache=$fromCache) -> ${names.joinToString()}"
                )
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                Util.showToast(this, "Error Firebase: ${e.message}")
            }
    }


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
            allProducts.filter {
                it.name.contains(query, ignoreCase = true)
            }
        }
        adapter.updateData(filtered)
    }

    private fun showDeleteDialog(product: Product) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar producto")
            .setMessage("¿Desea eliminar '${product.name}'? (solo UI por ahora)")
            .setPositiveButton("Eliminar") { _, _ ->
                allProducts = allProducts.filter { it.id != product.id }
                adapter.updateData(allProducts)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}