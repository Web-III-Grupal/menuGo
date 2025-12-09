package com.example.menugo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
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

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnGoRegister: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainLogin)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        etEmail = findViewById(R.id.etEmailLogin)
        etPassword = findViewById(R.id.etPasswordLogin)
        btnLogin = findViewById(R.id.btnLogin)
        btnGoRegister = findViewById(R.id.btnGoRegister)



        btnLogin.setOnClickListener { doLogin() }

        btnGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun doLogin() {
        val email = etEmail.text.toString().trim()
        val pass = etPassword.text.toString().trim()

        if (email.isEmpty() || pass.isEmpty()) {
            Util.showToast(this, "Correo y contraseña son obligatorios")
            return
        }

        auth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener
                loadUserAndGoMain(uid)
            }
            .addOnFailureListener {
                Util.showToast(this, "Error al iniciar sesión: ${it.message}")
            }
    }

    private fun loadUserAndGoMain(uid: String) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { snap ->
                if (!snap.exists()) {
                    Util.showToast(this, "Usuario sin perfil en Firestore")
                    return@addOnSuccessListener
                }

                // 🔹 Normalizamos el rol que viene de Firestore
                val roleFromDb = snap.getString("role")
                    ?.trim()
                    ?.lowercase()

                val finalRole = if (roleFromDb == "admin") {
                    UserRole.ROLE_ADMIN
                } else {
                    UserRole.ROLE_CLIENT
                }

                val user = AppUser(
                    uid = uid,
                    name = snap.getString("name") ?: "",
                    email = snap.getString("email") ?: "",
                    role = finalRole,
                    photoUrl = snap.getString("photoUrl")
                )

                val intent = Intent(this, MainActivity::class.java).apply {
                    putExtra(UserRole.EXTRA_USER_ROLE, user.role)   // aquí ya es "admin" o "client"
                }
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            .addOnFailureListener {
                Util.showToast(this, "Error cargando perfil: ${it.message}")
            }
    }

}
