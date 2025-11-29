package Entity

data class Product(
    var id: Int,
    var name: String,
    var description: String,
    var price: Double,
    var category: String,
    var imageUri: String? = null
)