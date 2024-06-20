package com.appmovil24.starproyect.activity.feed

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.appmovil24.starproyect.R
import com.appmovil24.starproyect.activity.Preferences
import com.appmovil24.starproyect.activity.PublishChallengePostForm
import com.appmovil24.starproyect.activity.UserProfile
import com.appmovil24.starproyect.repository.AuthenticationRepository
import com.appmovil24.starproyect.repository.ChallengePostRepository
import com.appmovil24.starproyect.databinding.ActivityFeedBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class Feed : AppCompatActivity() {

    private val chanllengeRepository = ChallengePostRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityFeedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser == null)
            AuthenticationRepository.logIn(this)

        binding.preferencesButton.setOnClickListener {
            val intent = Intent(this, Preferences::class.java)
            startActivity(intent)
        }
        binding.publishButton.setOnClickListener {
            val intent = Intent(this, PublishChallengePostForm::class.java)
            startActivity(intent)
        }
        binding.userProfileButton.setOnClickListener {
            val intent = Intent(this, UserProfile::class.java)
            startActivity(intent)
        }

        fetchCompetencias()

    }

    private fun fetchCompetencias() {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val competenciasList = chanllengeRepository.getAll()

                // Obtener referencias a los elementos de tu XML
                val userNameTextView = findViewById<TextView>(R.id.user_name_text_view)
                val disciplineTextView = findViewById<TextView>(R.id.discipline_text_view)
                val pointsTextView = findViewById<TextView>(R.id.points_text_view)
                val starsTextView = findViewById<TextView>(R.id.stars_text_view)
                val competitionsListView = findViewById<ListView>(R.id.competitions_list_view)


                // Adaptador para la ListView (si deseas mostrar datos en la lista)
                val adapter = ArrayAdapter(this@Feed, android.R.layout.simple_list_item_1, competenciasList)
                competitionsListView.adapter = adapter
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
