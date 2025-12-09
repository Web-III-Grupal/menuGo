package com.example.menugo

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.menugo.Entity.Product
import com.example.menugo.Util.Util
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class AddProductActivity : AppCompatActivity() {

    // Views
    private lateinit var imgProduct: ImageView
    private lateinit var txtId: EditText
    private lateinit var txtName: EditText
    private lateinit var txtDescription: EditText
    private lateinit var txtCategory: EditText
    private lateinit var txtPrice: EditText
    private lateinit var btnSave: Button

    // Firebase
    private var db: FirebaseFirestore? = null
    private val storageRef by lazy { FirebaseStorage.getInstance().reference }

    // Imagen
    private val REQUEST_GALLERY = 1001
    private val REQUEST_CAMERA = 1002
    private var currentImageUri: Uri? = null
    private var currentCameraBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_product)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializar Firebase de forma segura
        try {
            FirebaseApp.initializeApp(this)
            db = FirebaseFirestore.getInstance()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            db = null
        }

        // Referencias a vistas
        imgProduct = findViewById(R.id.imgProductAdd)
        txtId = findViewById(R.id.txtID)
        txtName = findViewById(R.id.txtNameProduct)
        txtDescription = findViewById(R.id.txtDescription)
        txtCategory = findViewById(R.id.txtCategory)
        txtPrice = findViewById(R.id.txtPrice)
        btnSave = findViewById(R.id.button2)

        // Si viene categoría desde la lista, la rellenamos
        val categoryFromList = intent.getStringExtra("category")
        if (!categoryFromList.isNullOrBlank()) {
            txtCategory.setText(categoryFromList)
        }

        imgProduct.setOnClickListener { showImageSourceDialog() }
        btnSave.setOnClickListener { saveProduct() }
    }

    // ---------- Selección de imagen ----------

    private fun showImageSourceDialog() {
        val options = arrayOf("Galería", "Cámara")
        AlertDialog.Builder(this)
            .setTitle("Seleccionar imagen")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openGallery()
                    1 -> openCamera()
                }
            }
            .show()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        @Suppress("DEPRECATION")
        startActivityForResult(intent, REQUEST_GALLERY)
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            @Suppress("DEPRECATION")
            startActivityForResult(intent, REQUEST_CAMERA)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return

        when (requestCode) {
            REQUEST_GALLERY -> {
                val uri = data?.data
                if (uri != null) {
                    currentImageUri = uri
                    currentCameraBitmap = null
                    imgProduct.setImageURI(uri)
                }
            }
            REQUEST_CAMERA -> {
                val bitmap = data?.extras?.get("data") as? Bitmap
                if (bitmap != null) {
                    currentCameraBitmap = bitmap
                    currentImageUri = null
                    imgProduct.setImageBitmap(bitmap)
                }
            }
        }
    }

    // ---------- Guardar producto ----------

    private fun saveProduct() {
        val firestore = db
        if (firestore == null) {
            Util.showToast(this, "Firebase no disponible")
            return
        }

        val idText = txtId.text.toString().trim()
        val name = txtName.text.toString().trim()
        val description = txtDescription.text.toString().trim()
        val category = txtCategory.text.toString().trim()
        val priceText = txtPrice.text.toString().trim()

        if (name.isEmpty() || category.isEmpty() || priceText.isEmpty()) {
            Util.showToast(this, "Nombre, categoría y precio son obligatorios")
            return
        }

        val id = idText.toIntOrNull() ?: 0
        val price = priceText.toDoubleOrNull()
        if (price == null) {
            Util.showToast(this, "El precio no es válido")
            return
        }

        // Primero subimos la imagen (si hay), luego guardamos en Firestore
        if (currentImageUri != null) {
            uploadImageFromGalleryAndSave(currentImageUri!!, id, name, description, category, price)
        } else if (currentCameraBitmap != null) {
            uploadImageFromCameraAndSave(currentCameraBitmap!!, id, name, description, category, price)
        } else {
            // Sin imagen: guardamos el producto con imageUri = ""
            saveProductInFirestore(
                id = id,
                name = name,
                description = description,
                category = category,
                price = price,
                imageUrl = ""
            )
        }
    }

    private fun uploadImageFromGalleryAndSave(
        uri: Uri,
        id: Int,
        name: String,
        description: String,
        category: String,
        price: Double
    ) {
        val path = "products/${category.lowercase()}_${System.currentTimeMillis()}.jpg"
        val ref = storageRef.child(path)

        ref.putFile(uri)
            .continueWithTask { ref.downloadUrl }
            .addOnSuccessListener { downloadUri ->
                saveProductInFirestore(id, name, description, category, price, downloadUri.toString())
            }
            .addOnFailureListener {
                Util.showToast(this, "Error subiendo imagen: ${it.message}")
            }
    }

    private fun uploadImageFromCameraAndSave(
        bitmap: Bitmap,
        id: Int,
        name: String,
        description: String,
        category: String,
        price: Double
    ) {
        val path = "products/${category.lowercase()}_${System.currentTimeMillis()}.jpg"
        val ref = storageRef.child(path)

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos)
        val data = baos.toByteArray()

        ref.putBytes(data)
            .continueWithTask { ref.downloadUrl }
            .addOnSuccessListener { downloadUri ->
                saveProductInFirestore(id, name, description, category, price, downloadUri.toString())
            }
            .addOnFailureListener {
                Util.showToast(this, "Error subiendo imagen: ${it.message}")
            }
    }

    private fun saveProductInFirestore(
        id: Int,
        name: String,
        description: String,
        category: String,
        price: Double,
        imageUrl: String
    ) {
        val firestore = db ?: return

        val product = Product(
            id = id,
            name = name,
            description = description,
            price = price,
            category = category,
            imageUri = imageUrl
        )

        val productMap = hashMapOf(
            "id" to product.id,
            "name" to product.name,
            "description" to product.description,
            "price" to product.price,
            "category" to product.category,
            "imageUri" to (product.imageUri ?: "")
        )

        firestore.collection("products")
            .add(productMap)
            .addOnSuccessListener {
                Util.showToast(this, "Producto guardado")
                finish()
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                Util.showToast(this, "Error al guardar: ${e.message}")
            }
    }
}
