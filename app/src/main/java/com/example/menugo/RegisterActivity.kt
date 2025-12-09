package com.example.menugo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.menugo.Entity.AppUser

import com.example.menugo.Util.UserRole
import com.example.menugo.Util.Util
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText

    private lateinit var rgRole: RadioGroup
    private lateinit var rbClient: RadioButton
    private lateinit var rbAdmin: RadioButton

    private lateinit var btnRegister: Button
    private lateinit var btnGoLogin: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainRegister)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Inputs
        etName = findViewById(R.id.etNameRegister)
        etEmail = findViewById(R.id.etEmailRegister)
        etPassword = findViewById(R.id.etPasswordRegister)

        // Radio buttons
        rgRole = findViewById(R.id.rgRole)
        rbClient = findViewById(R.id.rbClient)
        rbAdmin = findViewById(R.id.rbAdmin)

        // Botones
        btnRegister = findViewById(R.id.btnRegister)
        btnGoLogin = findViewById(R.id.btnGoLogin)

        btnRegister.setOnClickListener { doRegister() }

        btnGoLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun doRegister() {
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val pass = etPassword.text.toString().trim()

        if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            Util.showToast(this, "Nombre, correo y contraseña son obligatorios")
            return
        }

        // Determinar rol según radio seleccionado
        val selectedRoleId = rgRole.checkedRadioButtonId
        val role = when (selectedRoleId) {
            R.id.rbAdmin -> UserRole.ROLE_ADMIN      // "admin"
            R.id.rbClient, -1 -> UserRole.ROLE_CLIENT // "client" (por defecto)
            else -> UserRole.ROLE_CLIENT
        }

        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener

                val user = AppUser(
                    uid = uid,
                    name = name,
                    email = email,
                    role = role
                )

                val userMap = hashMapOf(
                    "name" to user.name,
                    "email" to user.email,
                    "role" to user.role,
                    "photoUrl" to user.photoUrl
                )

                db.collection("users").document(uid).set(userMap)
                    .addOnSuccessListener {
                        Util.showToast(this, "Cuenta creada")

                        val intent = Intent(this, MainActivity::class.java).apply {
                            putExtra(UserRole.EXTRA_USER_ROLE, user.role)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                    }
                    .addOnFailureListener {
                        Util.showToast(this, "Error guardando usuario: ${it.message}")
                    }
            }
            .addOnFailureListener {
                Util.showToast(this, "Error registrando: ${it.message}")
            }
    }

}
