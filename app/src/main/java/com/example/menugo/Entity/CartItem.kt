package com.example.menugo.Entity

import com.example.menugo.Entity.Product
data class CartItem(
    val product: Product,
    var quantity: Int
) {
    val total: Double
        get() = product.price * quantity
}