package com.example.menugo.data

import com.example.menugo.Entity.Product
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseProductRepository {

    private val db = FirebaseFirestore.getInstance()
    private val productsRef = db.collection("products")

    /**
     * Carga TODOS los productos desde Firestore y los copia a ProductStore.products.
     */
    fun syncAllProducts(
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        productsRef.get()
            .addOnSuccessListener { snapshot ->
                ProductStore.products.clear()

                for (doc in snapshot.documents) {
                    val id = (doc.getLong("id") ?: 0L).toInt()
                    val name = doc.getString("name") ?: ""
                    val description = doc.getString("description") ?: ""
                    val price = doc.getDouble("price") ?: 0.0
                    val category = doc.getString("category") ?: ""
                    val imageUri = doc.getString("imageUri")

                    val product = Product(
                        id = id,
                        name = name,
                        description = description,
                        price = price,
                        category = category,
                        imageUri = imageUri
                    )
                    ProductStore.products.add(product)
                }

                onSuccess()
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }

    /**
     * Guarda un producto nuevo en Firestore.
     */
    fun addProduct(product: Product, onResult: (Boolean) -> Unit = {}) {
        val data = hashMapOf(
            "id" to product.id,
            "name" to product.name,
            "description" to product.description,
            "price" to product.price,
            "category" to product.category,
            "imageUri" to product.imageUri
        )

        productsRef.add(data)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }
}
