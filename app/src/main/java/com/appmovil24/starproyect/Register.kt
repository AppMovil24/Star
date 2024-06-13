package com.appmovil24.starproyect

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.SignInButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class Register : AppCompatActivity() {

    private lateinit var continueBtn : Button
    private lateinit var radioGroup: RadioGroup
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        setContentView(R.layout.register);
        radioGroup = findViewById(R.id.rg)
        continueBtn = findViewById(R.id.btn_continuar);
        continueBtn.setOnClickListener {
            saveSelection();
        }
    }

    private fun saveSelection() {
        val selectedId = radioGroup.checkedRadioButtonId

        if (selectedId == -1) {
            // No radio button is selected
            Toast.makeText(this, "Por favor, selecciona una opción", Toast.LENGTH_SHORT).show()
        } else {
            val selectedRadioButton = findViewById<RadioButton>(selectedId)
            val selection = selectedRadioButton.text.toString()

            // Guardar en Firebase
            val userId = firebaseAuth.currentUser?.uid
            if (userId != null) {
                val database = FirebaseDatabase.getInstance()
                val userRef = database.getReference("users").child(userId)

                userRef.child("selection").setValue(selection)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Éxito al guardar la selección
                            Toast.makeText(this, "Selección guardada exitosamente", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            // Error al guardar la selección
                            Toast.makeText(this, "Error al guardar la selección", Toast.LENGTH_SHORT).show()
                            signOut()
                            finish()
                        }
                    }
            } else {
                Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signOut() {
        Firebase.auth.signOut()
        logIn()
    }

    private fun logIn() {
        val intent = Intent(this, Login::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }
}