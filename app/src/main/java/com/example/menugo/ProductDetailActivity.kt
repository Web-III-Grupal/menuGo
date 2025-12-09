package com.example.menugo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.menugo.Entity.Product
import com.example.menugo.data.CartManager
import com.example.menugo.Util.Util

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var product: Product
    private var quantity: Int = 1

    private lateinit var imgProduct: ImageView
    private lateinit var txtName: TextView
    private lateinit var txtCategory: TextView
    private lateinit var txtDescription: TextView
    private lateinit var txtPrice: TextView
    private lateinit var txtQuantity: TextView
    private lateinit var btnDecrease: Button
    private lateinit var btnIncrease: Button
    private lateinit var btnAddToCart: Button
    //private lateinit var btnEditProduct: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_product_detail)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainProductDetail)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        imgProduct = findViewById(R.id.imgProductDetail)
        txtName = findViewById(R.id.txtProductNameDetail)
        txtCategory = findViewById(R.id.txtProductCategoryDetail)
        txtDescription = findViewById(R.id.txtProductDescriptionDetail)
        txtPrice = findViewById(R.id.txtProductPriceDetail)
        txtQuantity = findViewById(R.id.txtQuantity)
        btnDecrease = findViewById(R.id.btnDecrease)
        btnIncrease = findViewById(R.id.btnIncrease)
        btnAddToCart = findViewById(R.id.btnAddToCart)
       // btnEditProduct = findViewById(R.id.btnEditProduct)

        // 👉 Recuperamos TODOS los datos que mandó ProductListActivity
        val id = intent.getIntExtra("id", 0)
        val name = intent.getStringExtra("name") ?: ""
        val description = intent.getStringExtra("description") ?: ""
        val price = intent.getDoubleExtra("price", 0.0)
        val category = intent.getStringExtra("category") ?: ""
        val imageUri = intent.getStringExtra("imageUri") ?: ""

        product = Product(id, name, description, price, category, imageUri)

        bindProduct()

        btnDecrease.setOnClickListener {
            if (quantity > 1) {
                quantity--
                txtQuantity.text = quantity.toString()
            }
        }

        btnIncrease.setOnClickListener {
            quantity++
            txtQuantity.text = quantity.toString()
        }

        btnAddToCart.setOnClickListener {
            CartManager.addProduct(product, quantity)
            Util.showToast(this, "Añadido al carrito (${quantity})")
        }

        //btnEditProduct.setOnClickListener {
            // Si quieres seguir usando edición, puedes pasar también los datos aquí
          //  val intent = Intent(this, EditProductActivity::class.java).apply {
            //    putExtra("id", product.id)
              //  putExtra("name", product.name)
                //putExtra("description", product.description)
                //putExtra("price", product.price)
                //putExtra("category", product.category)
                //putExtra("imageUri", product.imageUri)
            //}
            //startActivity(intent)
        //}
    }

    override fun onResume() {
        super.onResume()
        // Por ahora simplemente volvemos a pintar los datos actuales
        bindProduct()
    }

    private fun bindProduct() {
        imgProduct.setImageResource(android.R.drawable.ic_menu_gallery)
        txtName.text = product.name
        txtCategory.text = "Categoría: ${product.category}"
        txtDescription.text = product.description
        txtPrice.text = Util.formatPrice(product.price)
        txtQuantity.text = quantity.toString()
    }
}
