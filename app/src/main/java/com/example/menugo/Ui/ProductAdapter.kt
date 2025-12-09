package com.example.menugo.Ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.menugo.Entity.Product
import com.example.menugo.R
import com.example.menugo.Util.Util   // <- ojo: package en minúscula

class ProductAdapter(
    private var products: List<Product>,
    private val onItemClick: (Product) -> Unit,
    // Long click es opcional: sólo se usa en modo admin
    private val onItemLongClick: ((Product) -> Unit)? = null
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val imgProduct: ImageView = itemView.findViewById(R.id.imgProduct)
        private val txtProductName: TextView = itemView.findViewById(R.id.txtProductName)
        private val txtProductDescription: TextView = itemView.findViewById(R.id.txtProductDescription)
        private val txtProductPrice: TextView = itemView.findViewById(R.id.txtProductPrice)

        fun bind(product: Product) {
            txtProductName.text = product.name
            txtProductDescription.text = product.description
            txtProductPrice.text = Util.formatPrice(product.price)

            // Cargar imagen desde URL (Firebase Storage) con Glide
            if (!product.imageUri.isNullOrBlank()) {
                Glide.with(itemView.context)
                    .load(product.imageUri)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(imgProduct)
            } else {
                imgProduct.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            // Click normal -> ver detalle / agregar al carrito
            itemView.setOnClickListener {
                onItemClick(product)
            }

            // Long click solo si nos pasaron lambda (modo admin)
            if (onItemLongClick != null) {
                itemView.setOnLongClickListener {
                    onItemLongClick?.invoke(product)
                    true
                }
            } else {
                // En modo cliente no queremos long-click
                itemView.setOnLongClickListener(null)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size

    fun updateData(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }
}
