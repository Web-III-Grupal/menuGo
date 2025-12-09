package com.example.menugo.data

import com.example.menugo.Entity.Product
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseProductRepository {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    /**
     * Carga todos los productos desde Firestore y los convierte a la clase Product.
     */
    fun syncAllProducts(
        onSuccess: (List<Product>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        db.collection("products")
            .get()
            .addOnSuccessListener { snapshot ->
                val products = snapshot.documents.mapNotNull { doc ->
                    val id = (doc.getLong("id") ?: 0L).toInt()
                    val name = doc.getString("name") ?: return@mapNotNull null
                    val description = doc.getString("description") ?: ""
                    val price = doc.getDouble("price") ?: 0.0
                    val category = doc.getString("category") ?: ""
                    val imageUri = doc.getString("imageUri") ?: ""   // <- AQUÍ FORZAMOS String

                    Product(
                        id = id,
                        name = name,
                        description = description,
                        price = price,
                        category = category,
                        imageUri = imageUri        // ya es String, no String?
                    )
                }

                onSuccess(products)
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }

    /**
     * Guarda un producto nuevo en Firestore.
     */
    fun addProduct(
        product: Product,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val productMap = hashMapOf(
            "id" to product.id,
            "name" to product.name,
            "description" to product.description,
            "price" to product.price,
            "category" to product.category,
            // product.imageUri ya es String (NO nullable)
            "imageUri" to product.imageUri
        )

        db.collection("products")
            .add(productMap)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e) }
    }
}
