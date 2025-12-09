package com.example.menugo

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.menugo.Entity.Product
import com.example.menugo.Util.Util
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class EditProductActivity : AppCompatActivity() {

    // Views
    private lateinit var imgProduct: ImageView
    private lateinit var txtId: EditText
    private lateinit var txtName: EditText
    private lateinit var txtDescription: EditText
    private lateinit var txtCategory: EditText
    private lateinit var txtPrice: EditText
    private lateinit var btnSave: Button

    // Firebase
    private var db: FirebaseFirestore? = null
    private var docId: String? = null   // id del documento en Firestore

    // id lógico del producto (campo "id" en Firestore)
    private var productId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_product)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Recibir id del producto
        productId = intent.getIntExtra("productId", -1)
        if (productId == -1) {
            Util.showToast(this, "Producto inválido")
            finish()
            return
        }

        // Inicializar Firebase
        try {
            FirebaseApp.initializeApp(this)
            db = FirebaseFirestore.getInstance()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            db = null
        }

        // Referencias a vistas (ajusta ids si en tu XML se llaman distinto)
        imgProduct = findViewById(R.id.imageView)
        txtId = findViewById(R.id.txtID)
        txtName = findViewById(R.id.txtNameProduct)
        txtDescription = findViewById(R.id.txtDescription)
        txtCategory = findViewById(R.id.txtCategory)
        txtPrice = findViewById(R.id.txtPrice)
        btnSave = findViewById(R.id.button2)   // o el id de tu botón de "Guardar cambios"

        // Cargar datos desde Firestore
        loadProductFromFirestore()

        btnSave.setOnClickListener {
            updateProduct()
        }
    }

    private fun loadProductFromFirestore() {
        val firestore = db
        if (firestore == null) {
            Util.showToast(this, "Firebase no disponible")
            finish()
            return
        }

        firestore.collection("products")
            .whereEqualTo("id", productId)
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    Util.showToast(this, "Producto no encontrado")
                    finish()
                    return@addOnSuccessListener
                }

                val doc = snapshot.documents.first()
                docId = doc.id
                val data = doc.data ?: emptyMap<String, Any?>()

                val id = (data["id"] as? Number)?.toInt() ?: 0
                val name = (data["name"] ?: data["name "] ?: "").toString()
                val description = (data["description"] ?: data["descripcion"] ?: "").toString()
                val category = (data["category"] ?: data["categoria"] ?: "").toString()
                val price = (data["price"] as? Number)?.toDouble() ?: 0.0

                // Rellenar campos
                txtId.setText(id.toString())
                txtName.setText(name)
                txtDescription.setText(description)
                txtCategory.setText(category)
                txtPrice.setText(price.toString())
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                Util.showToast(this, "Error al cargar: ${e.message}")
                finish()
            }
    }

    private fun updateProduct() {
        val firestore = db
        val currentDocId = docId
        if (firestore == null || currentDocId == null) {
            Util.showToast(this, "No se puede actualizar (sin docId)")
            return
        }

        // Leer valores del formulario
        val idText = txtId.text.toString().trim()
        val name = txtName.text.toString().trim()
        val description = txtDescription.text.toString().trim()
        val category = txtCategory.text.toString().trim()
        val priceText = txtPrice.text.toString().trim()

        if (name.isEmpty() || category.isEmpty() || priceText.isEmpty()) {
            Util.showToast(this, "Nombre, categoría y precio son obligatorios")
            return
        }

        val id = idText.toIntOrNull() ?: 0
        val price = priceText.toDoubleOrNull()
        if (price == null) {
            Util.showToast(this, "El precio no es válido")
            return
        }

        val product = Product(
            id = id,
            name = name,
            description = description,
            price = price,
            category = category,
            imageUri = ""
        )

        val productMap = hashMapOf(
            "id" to product.id,
            "name" to product.name,
            "description" to product.description,
            "price" to product.price,
            "category" to product.category,
            "imageUri" to (product.imageUri ?: "")
        )

        firestore.collection("products")
            .document(currentDocId)
            .update(productMap as Map<String, Any>)
            .addOnSuccessListener {
                Util.showToast(this, "Producto actualizado")
                finish()
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                Util.showToast(this, "Error al actualizar: ${e.message}")
            }
    }
}
