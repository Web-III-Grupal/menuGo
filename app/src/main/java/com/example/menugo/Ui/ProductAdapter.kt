package com.example.menugo.Ui

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.menugo.Entity.Product
import com.example.menugo.R
import com.example.menugo.util.Util
class ProductAdapter(
    private var products: List<Product>,
    private val onItemClick: (Product) -> Unit,
    private val onItemLongClick: (Product) -> Unit
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
            if (product.imageUri != null) {
                imgProduct.setImageURI(Uri.parse(product.imageUri))
            } else {
                imgProduct.setImageResource(android.R.drawable.ic_menu_gallery)
            }
            itemView.setOnClickListener {
                onItemClick(product)
            }

            itemView.setOnLongClickListener {
                onItemLongClick(product)
                true
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