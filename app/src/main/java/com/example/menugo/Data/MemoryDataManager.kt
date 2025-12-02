package com.example.menugo.data

import com.example.menugo.Entity.Product


class MemoryDataManager : IDataManager<Product> {

    private val itemList = mutableListOf<Product>()

    override fun add(item: Product) {
        itemList.add(item)
    }

    override fun getAll(): List<Product> {
        // devolvemos una copia para no exponer la lista interna
        return itemList.toList()
    }

    override fun update(item: Product) {
        val index = itemList.indexOfFirst { it.id == item.id }
        if (index != -1) {
            itemList[index] = item
        }
    }

    override fun delete(id: Int) {
        itemList.removeIf { it.id == id }
    }
}
