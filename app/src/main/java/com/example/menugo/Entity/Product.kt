package com.example.menugo.Entity

import java.io.Serializable

data class Product(
    val id: Int = 0,
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val category: String = "",
    val imageUri: String? = null   // aquí guardaremos el downloadUrl de Storage
)
