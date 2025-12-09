package com.example.menugo.data

import com.example.menugo.Entity.CartItem
import com.example.menugo.Entity.Product

object CartManager {

    private val items = mutableListOf<CartItem>()

    fun addProduct(product: Product, quantity: Int) {
        val existing = items.find { it.product.id == product.id }
        if (existing != null) {
            existing.quantity += quantity
        } else {
            items.add(CartItem(product, quantity))
        }
    }

    fun getItems(): List<CartItem> = items

    fun getTotal(): Double =
        items.sumOf { it.product.price * it.quantity }

    fun clear() {
        items.clear()
    }
}
