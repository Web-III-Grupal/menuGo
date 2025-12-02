package com.example.menugo.controller

import com.example.menugo.Entity.Product
import com.example.menugo.data.IDataManager


class ProductController(private val dataManager: IDataManager<Product>) {

    fun addProduct(product: Product) {
        dataManager.add(product)
    }

    fun getAllProducts(): List<Product> {
        return dataManager.getAll()
    }

    fun updateProduct(product: Product) {
        dataManager.update(product)
    }

    fun deleteProduct(id: Int) {
        dataManager.delete(id)
    }

    fun findProductByName(name: String): List<Product> {
        return dataManager.getAll().filter {
            it.name.contains(name, ignoreCase = true)
        }
    }
}
