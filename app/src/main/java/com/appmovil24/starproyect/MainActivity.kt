package com.appmovil24.starproyect

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.appmovil24.starproyect.Model.Usuario
import com.appmovil24.starproyect.Repo.CompetenciaRepo
import com.appmovil24.starproyect.Repo.UserRepo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var signOutButton: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var userNameTextView: TextView
    private lateinit var disciplineTextView: TextView
    private lateinit var pointsTextView: TextView
    private lateinit var competitionsListView: ListView
    private lateinit var competeButton: Button
    private val competenciaRepo = CompetenciaRepo()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar los botones y vistas
        signOutButton = findViewById(R.id.sign_out_button)
        auth = FirebaseAuth.getInstance()

        // Verificar si el usuario está autenticado
        if (auth.currentUser == null) {
            logIn()
            return
        }

        val userRepositorio = UserRepo(auth.currentUser!!)
        userNameTextView = findViewById(R.id.user_name_text_view)
        disciplineTextView = findViewById(R.id.discipline_text_view)
        pointsTextView = findViewById(R.id.points_text_view)
        competitionsListView = findViewById(R.id.competitions_list_view)
        competeButton = findViewById(R.id.compete_button)
        signOutButton = findViewById(R.id.sign_out_button)

        // Método para obtener el usuario y mostrar los datos
        userRepositorio.getUser { usuario: Usuario? ->
            if (usuario != null) {
                runOnUiThread {
                    displayUserInfo(usuario)
                }
            } else {
                runOnUiThread {
                    logIn()
                }
            }
        }

        // Acción al hacer clic en el botón de cerrar sesión
        signOutButton.setOnClickListener {
            signOut()
        }

        // Obtener las competencias y mostrarlas en el ListView
        fetchCompetencias()

        // Acción al hacer clic en el botón de competir
        competeButton.setOnClickListener {
            // Aquí iría el código para abrir la actividad de competir
            // startActivity(Intent(this, CompeteActivity::class.java))
        }
    }

    private fun fetchCompetencias() {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val competenciasList = competenciaRepo.getAll()

                // Obtener referencias a los elementos de tu XML
                val userNameTextView = findViewById<TextView>(R.id.user_name_text_view)
                val disciplineTextView = findViewById<TextView>(R.id.discipline_text_view)
                val pointsTextView = findViewById<TextView>(R.id.points_text_view)
                val starsTextView = findViewById<TextView>(R.id.stars_text_view)
                val competitionsListView = findViewById<ListView>(R.id.competitions_list_view)


                // Adaptador para la ListView (si deseas mostrar datos en la lista)
                val adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_list_item_1, competenciasList)
                competitionsListView.adapter = adapter
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun displayUserInfo(user: Usuario) {
        userNameTextView.text = "${user.nombre} ${user.apellido}"
        disciplineTextView.text = "Disciplina: ${user.disciplina}"
        val totalPoints = user.competencias * user.puntos
        pointsTextView.text = "Puntos: $totalPoints"
    }

    private fun signOut() {
        Firebase.auth.signOut()
        logIn()
    }

    private fun logIn() {
        val intent = Intent(this, Login::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish() // Finish MainActivity so user can't return to it
    }
}
