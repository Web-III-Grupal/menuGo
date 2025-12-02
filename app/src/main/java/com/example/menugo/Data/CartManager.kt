package com.example.menugo.data

import com.example.menugo.Entity.CartItem
import com.example.menugo.Entity.Product

object CartManager {

    private val _items = mutableListOf<CartItem>()
    val items: List<CartItem>
        get() = _items

    fun addProduct(product: Product, quantity: Int = 1) {
        val existing = _items.find { it.product.id == product.id }
        if (existing != null) {
            existing.quantity += quantity
        } else {
            _items.add(CartItem(product, quantity))
        }
    }

    fun removeProduct(productId: Int) {
        _items.removeAll { it.product.id == productId }
    }

    fun clear() {
        _items.clear()
    }

    fun getTotal(): Double {
        return _items.sumOf { it.total }
    }

    fun isEmpty(): Boolean = _items.isEmpty()
}
