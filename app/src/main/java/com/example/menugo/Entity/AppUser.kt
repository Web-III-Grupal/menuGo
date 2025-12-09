package com.example.menugo.Entity

data class AppUser(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "client",
    val photoUrl: String? = null
)

