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
import com.example.menugo.data.CartManager
import com.example.menugo.Ui.CartAdapter
import com.example.menugo.util.Util

class CartActivity : AppCompatActivity() {

    private lateinit var rvCart: RecyclerView
    private lateinit var txtCartTotal: TextView
    private lateinit var btnConfirmOrder: Button
    private lateinit var adapter: CartAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cart)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainCart)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        rvCart = findViewById(R.id.rvCart)
        txtCartTotal = findViewById(R.id.txtCartTotal)
        btnConfirmOrder = findViewById(R.id.btnConfirmOrder)

        adapter = CartAdapter(CartManager.items)
        rvCart.layoutManager = LinearLayoutManager(this)
        rvCart.adapter = adapter

        updateTotal()

        btnConfirmOrder.setOnClickListener {
            if (CartManager.isEmpty()) {
                Util.showToast(this, "El carrito está vacío")
            } else {
                showConfirmDialog()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        adapter.updateData(CartManager.items)
        updateTotal()
    }

    private fun updateTotal() {
        val total = CartManager.getTotal()
        txtCartTotal.text = "Total: ${Util.formatPrice(total)}"
    }

    private fun showConfirmDialog() {
        val total = CartManager.getTotal()
        AlertDialog.Builder(this)
            .setTitle("Confirmar pedido")
            .setMessage("¿Desea confirmar el pedido por ${Util.formatPrice(total)}?")
            .setPositiveButton("Confirmar") { _, _ ->
                CartManager.clear()
                adapter.updateData(CartManager.items)
                updateTotal()
                Util.showToast(this, "Pedido realizado")
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
