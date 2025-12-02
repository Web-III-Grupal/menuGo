package com.example.menugo.data

interface IDataManager<T> {
    fun add(item: T)
    fun getAll(): List<T>
    fun update(item: T)
    fun delete(id: Int)
}