package com.example.menugo

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.menugo.Entity.Product
import com.example.menugo.data.FirebaseProductRepository
import com.example.menugo.data.ProductStore
import com.example.menugo.util.Util

class AddProductActivity : AppCompatActivity() {

    private lateinit var txtId: EditText
    private lateinit var txtName: EditText
    private lateinit var txtDescription: EditText
    private lateinit var txtCategory: EditText
    private lateinit var txtPrice: EditText
    private lateinit var btnSave: Button
    private lateinit var imgProductAdd: ImageView
    private lateinit var btnSelectImageAdd: Button
    private lateinit var btnSearchId: ImageButton

    private var selectedImageUri: Uri? = null

    private val firebaseRepo = FirebaseProductRepository()

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            imgProductAdd.setImageURI(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_product)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        txtId = findViewById(R.id.txtID)
        txtName = findViewById(R.id.txtNameProduct)
        txtDescription = findViewById(R.id.txtDescription)
        txtCategory = findViewById(R.id.txtCategory)
        txtPrice = findViewById(R.id.txtPrice)
        btnSave = findViewById(R.id.button2)
        imgProductAdd = findViewById(R.id.imgProductAdd)
        btnSelectImageAdd = findViewById(R.id.btnSelectImageAdd)
        btnSearchId = findViewById(R.id.btnSearchId_product)

        val newId = ProductStore.nextId()
        txtId.setText(newId.toString())
        txtId.isEnabled = false

        val categoryFromIntent = intent.getStringExtra("category")
        if (!categoryFromIntent.isNullOrBlank()) {
            txtCategory.setText(categoryFromIntent)
        }

        btnSelectImageAdd.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnSave.setOnClickListener {
            val name = txtName.text.toString().trim()
            val description = txtDescription.text.toString().trim()
            val category = txtCategory.text.toString().trim()
            val priceText = txtPrice.text.toString().trim()
            val price = priceText.toDoubleOrNull() ?: 0.0

            if (!Util.validateProduct(name, description, price)) {
                Util.showToast(this, "Completa nombre, descripción y precio mayor a 0")
                return@setOnClickListener
            }

            val product = Product(
                id = newId,
                name = name,
                description = description,
                price = price,
                category = if (category.isBlank()) "Sin categoría" else category,
                imageUri = selectedImageUri?.toString()
            )

            // 1) Guardar local
            ProductStore.products.add(product)

            // 2) Guardar en Firebase
            firebaseRepo.addProduct(product) { ok ->
                if (!ok) {
                    Util.showToast(this, "Guardado localmente, pero falló en Firebase")
                }
            }

            Util.showToast(this, "Producto agregado")
            finish()
        }

        btnSearchId.setOnClickListener {
            Util.showToast(this, "Buscar por ID (pendiente)")
        }
    }
}