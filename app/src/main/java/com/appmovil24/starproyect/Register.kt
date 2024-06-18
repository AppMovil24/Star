package com.appmovil24.starproyect

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.appmovil24.starproyect.Model.Usuario
import com.appmovil24.starproyect.Repo.UserRepo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class Register : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var userRepo: UserRepo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register)
        auth = FirebaseAuth.getInstance()
        userRepo = UserRepo(auth.currentUser)

        val etUsuarioId: EditText = findViewById(R.id.etUsuarioId)
        val etNombre: EditText = findViewById(R.id.etNombre)
        val etApellido: EditText = findViewById(R.id.etApellido)
        val cbCompetidor: CheckBox = findViewById(R.id.cbCompetidor)
        val cbFiscalizador: CheckBox = findViewById(R.id.cbFiscalizador)
        val spDisciplina: Spinner = findViewById(R.id.spDisciplina)
        val btnRegister: Button = findViewById(R.id.btnRegister)

        // Configurar el Spinner para la disciplina
        val disciplinas = arrayOf("Tenis", "Boxeo", "Paddle", "Ajedrez")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, disciplinas)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spDisciplina.adapter = adapter

        btnRegister.setOnClickListener {
            val usuarioId = etUsuarioId.text.toString()
            val nombre = etNombre.text.toString()
            val apellido = etApellido.text.toString()
            val competidor = cbCompetidor.isChecked
            val fiscalizador = cbFiscalizador.isChecked
            val disciplina = spDisciplina.selectedItem.toString()
            val email = auth.currentUser?.email ?: ""
            // Verificar si el usuarioId ya existe
            userRepo.existe(usuarioId) { existe ->
                if (!existe) {
                    val newUser = Usuario(
                        usuarioId = usuarioId,
                        nombre = nombre,
                        apellido = apellido,
                        email = email,
                        competidor = competidor,
                        fiscalizador = fiscalizador,
                        disciplina = disciplina,
                        puntos = 0
                    )
                    createUser(newUser)
                } else {
                    // Mostrar mensaje al usuario de que el usuarioId no está disponible
                    Toast.makeText(this, "El usuarioId ya está en uso, elige otro.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun createUser(user: Usuario) {
        userRepo.createUser(user) { success ->
            if (success) {
                // Usuario creado exitosamente, redirigir a MainActivity
                Log.d("RegisterActivity", "Usuario registrado correctamente")
                val intent = Intent(this@Register, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            } else {
                // Manejar el error de creación del usuario
                Log.e("RegisterActivity", "Error al registrar el usuario en Firestore")
            }
        }
    }

    private fun signOut() {
        Firebase.auth.signOut()
        logIn()
    }

    private fun logIn() {
        val intent = Intent(this@Register, Login::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}
