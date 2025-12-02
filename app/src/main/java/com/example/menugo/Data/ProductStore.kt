package com.example.menugo.data

import com.example.menugo.Entity.Product

object ProductStore {

    val products = mutableListOf(
        Product(1, "Hamburguesa Clásica", "Carne, queso y lechuga", 2500.0, "Hamburguesas"),
        Product(2, "Hamburguesa Doble", "Doble carne y queso", 3200.0, "Hamburguesas"),
        Product(3, "Coca-Cola 500ml", "Bebida gaseosa", 900.0, "Bebidas"),
        Product(4, "Fresca 500ml", "Bebida gaseosa", 900.0, "Bebidas"),
        Product(5, "Sundae Chocolate", "Helado con topping", 1500.0, "Postres"),
        Product(6, "Cono Vainilla", "Cono sencillo", 700.0, "Postres")
    )

    fun nextId(): Int = (products.maxOfOrNull { it.id } ?: 0) + 1
}
