package com.example.menugo

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.menugo.Entity.Product
import com.example.menugo.data.ProductStore
import com.example.menugo.util.Util

class EditProductActivity : AppCompatActivity() {

    private lateinit var txtId: EditText
    private lateinit var txtName: EditText
    private lateinit var txtDescription: EditText
    private lateinit var txtCategory: EditText
    private lateinit var txtPrice: EditText
    private lateinit var btnSave: Button

    private var product: Product? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_product)

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

        val productId = intent.getIntExtra("productId", -1)
        product = ProductStore.products.find { it?.id == productId }

        if (product == null) {
            Util.showToast(this, "Producto no encontrado")
            finish()
            return
        }

        // Cargar datos
        txtId.setText(product!!.id.toString())
        txtId.isEnabled = false
        txtName.setText(product!!.name)
        txtDescription.setText(product!!.description)
        txtCategory.setText(product!!.category)
        txtPrice.setText(product!!.price.toString())

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

            product!!.name = name
            product!!.description = description
            product!!.category = if (category.isBlank()) "Sin categoría" else category
            product!!.price = price

            Util.showToast(this, "Producto actualizado")
            finish()
        }
    }
}
