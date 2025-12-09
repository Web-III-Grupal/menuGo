package com.example.menugo.Util

import android.content.Context
import android.content.SharedPreferences
import com.example.menugo.Util.UserRole
import com.example.menugo.Entity.AppUser
/**
 * Maneja datos simples de sesión (por ahora sólo el rol del usuario).
 * Lo usamos en RegisterActivity / LoginActivity si queremos guardar el rol localmente.
 */
class SessionManager(context: Context) {
    object SessionManager {
        var currentUser: AppUser? = null
    }
    private val prefs: SharedPreferences =
        context.getSharedPreferences("menugo_session_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ROLE = "user_role"
    }

    /**
     * Guarda el rol del usuario (admin / client)
     */
    fun saveUserRole(role: String) {
        prefs.edit().putString(KEY_ROLE, role).apply()
    }

    /**
     * Obtiene el rol guardado. Si no hay nada, devuelve CLIENT por defecto.
     */
    fun getUserRole(defaultRole: String = UserRole.ROLE_CLIENT): String {
        return prefs.getString(KEY_ROLE, defaultRole) ?: defaultRole
    }

    /**
     * Limpia todos los datos de sesión.
     */
    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
