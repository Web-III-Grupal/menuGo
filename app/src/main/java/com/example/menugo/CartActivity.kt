package com.example.menugo

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.menugo.Ui.CartAdapter
import com.example.menugo.data.CartManager
import com.example.menugo.Util.Util
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class CartActivity : AppCompatActivity() {

    private lateinit var rvCart: RecyclerView
    private lateinit var txtCartTotal: TextView
    private lateinit var btnConfirmOrder: Button
    private lateinit var adapter: CartAdapter

    private var db: FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cart)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainCart)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Firebase seguro
        try {
            FirebaseApp.initializeApp(this)
            db = FirebaseFirestore.getInstance()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            db = null
        }

        rvCart = findViewById(R.id.rvCart)
        txtCartTotal = findViewById(R.id.txtCartTotal)
        btnConfirmOrder = findViewById(R.id.btnConfirmOrder)

        adapter = CartAdapter(CartManager.getItems())
        rvCart.layoutManager = LinearLayoutManager(this)
        rvCart.adapter = adapter

        updateCartUi()

        btnConfirmOrder.setOnClickListener {
            if (CartManager.getItems().isEmpty()) {
                Util.showToast(this, "El carrito está vacío")
                return@setOnClickListener
            }
            showConfirmDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        updateCartUi()
    }

    private fun updateCartUi() {
        val items = CartManager.getItems()
        adapter.updateItems(items)

        val total = CartManager.getTotal()
        txtCartTotal.text = "Total: ${Util.formatPrice(total)}"
    }

    private fun showConfirmDialog() {
        val items = CartManager.getItems()
        val total = CartManager.getTotal()

        val resumen = buildString {
            append("¿Confirmar pedido?\n\n")
            items.forEach {
                append("• ${it.product.name} x${it.quantity}\n")
            }
            append("\nTotal: ${Util.formatPrice(total)}")
        }

        AlertDialog.Builder(this)
            .setTitle("Confirmación de pedido")
            .setMessage(resumen)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Confirmar") { _, _ ->
                sendOrderToFirebase()
            }
            .show()
    }

    private fun sendOrderToFirebase() {
        val firestore = db
        if (firestore == null) {
            Util.showToast(this, "Firebase no disponible")
            return
        }

        val items = CartManager.getItems()
        val total = CartManager.getTotal()

        val orderItems = items.map {
            mapOf(
                "productId" to it.product.id,
                "name" to it.product.name,
                "quantity" to it.quantity,
                "unitPrice" to it.product.price,
                "lineTotal" to it.product.price * it.quantity
            )
        }

        val orderMap = hashMapOf(
            "createdAt" to FieldValue.serverTimestamp(),
            "items" to orderItems,
            "total" to total
        )

        firestore.collection("orders")
            .add(orderMap)
            .addOnSuccessListener {
                Util.showToast(this, "Pedido enviado correctamente")
                CartManager.clear()
                updateCartUi()
                finish()
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                Util.showToast(this, "Error al enviar pedido: ${e.message}")
            }
    }
}
