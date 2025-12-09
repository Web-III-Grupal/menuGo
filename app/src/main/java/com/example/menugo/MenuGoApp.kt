package com.example.menugo

import android.app.Application
import com.google.firebase.FirebaseApp

class MenuGoApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Inicializar Firebase una sola vez para todo el proceso
        FirebaseApp.initializeApp(this)
    }
}
