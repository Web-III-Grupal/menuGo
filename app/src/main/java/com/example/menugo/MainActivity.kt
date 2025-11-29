package com.example.menugo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.menugo.Controller.ProductoController
import com.example.menugo.Data.MemoryDataManager
import com.example.menugo.Model.Producto
import com.example.menugo.Util.Util
import com.example.menugo.R

class MainActivity : AppCompatActivity() {

    private lateinit var controller: ProductoController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        controller = ProductoController(MemoryDataManager())

        // Datos de prueba
        controller.agregarProducto(
            Producto(1, "Hamburguesa Clásica", "Carne, queso y lechuga", 2500.0, R.drawable.ic_launcher_foreground)
        )
        controller.agregarProducto(
            Producto(2, "Papas Fritas", "Porción mediana", 1200.0, R.drawable.ic_launcher_background)
        )

        val lista = controller.obtenerProductos()

        Util.mostrarMensaje(this, "Productos cargados: ${lista.size}")
    }
}