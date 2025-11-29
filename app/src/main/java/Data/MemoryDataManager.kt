package Data

import Entity.Product

class MemoryDataManager<T> : IDataManager<T> {

    private val itemList = mutableListOf<T>()

    override fun add(item: T) {
        itemList.add(item)
    }

    override fun getAll(): List<T> {
        return itemList.toList()
    }

    override fun update(item: T) {
        val index = itemList.indexOfFirst { it == item }
        if (index != -1) itemList[index] = item
    }

    override fun delete(id: Int) {
        if (itemList.isNotEmpty() && itemList.first() is Product) {
            val list = itemList as MutableList<Product>
            list.removeIf { it.id == id }
        }
    }
}