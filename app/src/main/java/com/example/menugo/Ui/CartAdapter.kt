package com.example.menugo.Ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.menugo.Entity.CartItem
import com.example.menugo.R
import com.example.menugo.Util.Util

class CartAdapter(
    items: List<CartItem>
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    // Usamos una lista mutable interna para poder actualizarla
    private val items = mutableListOf<CartItem>().apply {
        addAll(items)
    }

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtName: TextView = itemView.findViewById(R.id.txtCartItemName)
        private val txtTotal: TextView = itemView.findViewById(R.id.txtCartItemTotal)

        fun bind(item: CartItem) {
            // Nombre + cantidad
            txtName.text = "${item.product.name} x${item.quantity}"

            // Total de esa línea: precio * cantidad
            val lineTotal = item.product.price * item.quantity

            // Mostrar total formateado
            txtTotal.text = Util.formatPrice(lineTotal)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            // Si tu layout de fila se llama distinto, cámbialo aquí
            .inflate(R.layout.activity_item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    // Método que usa CartActivity
    fun updateItems(newItems: List<CartItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
