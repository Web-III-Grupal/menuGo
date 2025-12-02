package com.example.menugo.util

import android.content.Context
import android.widget.Toast

object Util {

    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun formatPrice(price: Double): String {
        return "₡" + String.format("%.2f", price)
    }

    fun validateProduct(name: String, description: String, price: Double): Boolean {
        return name.isNotBlank() && description.isNotBlank() && price > 0
    }
}
