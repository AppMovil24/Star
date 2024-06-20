package com.appmovil24.starproyect.activity

import android.R
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.appmovil24.starproyect.activity.feed.Feed
import com.appmovil24.starproyect.databinding.ActivityPublishChallengePostBinding
import com.appmovil24.starproyect.enum.ChallengePostState
import com.appmovil24.starproyect.model.ChallengePostDTO
import com.appmovil24.starproyect.model.UserAccountDTO
import com.appmovil24.starproyect.repository.ChallengePostRepository
import com.appmovil24.starproyect.repository.UserAccountRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PublishChallengePostForm : AppCompatActivity() {

    private lateinit var binding : ActivityPublishChallengePostBinding
    private lateinit var userAccountRepository : UserAccountRepository
    private lateinit var challengePostRepository : ChallengePostRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userAccountRepository = UserAccountRepository(FirebaseAuth.getInstance().currentUser!!)
        challengePostRepository = ChallengePostRepository()

        binding = ActivityPublishChallengePostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val disciplines = listOf("Discipline 1", "Discipline 2", "Discipline 3")
        val disciplinesAdapter = ArrayAdapter(this, R.layout.simple_spinner_item, disciplines)
        disciplinesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.inputDiscipline.adapter = disciplinesAdapter

        binding.publishChallengePostButton.setOnClickListener {
            lifecycleScope.launch {
                publishCompetition()
            }
        }
    }

    suspend fun publishCompetition() {
        val date = binding.inputDate.text.toString().trim()
        val discipline = binding.inputDiscipline.selectedItem as String
        val location = GeoPoint(37.7749, -122.4194); // hay que obtener de un mapa de google seleccionado por el usuario

        if (date.isEmpty() || discipline.isEmpty() || location == null) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        userAccountRepository.get { userAccountDTO ->
            if (userAccountDTO != null) {
                lifecycleScope.launch {
                    challengePostRepository.add(ChallengePostDTO(
                        ChallengePostState.OPEN.name,
                        discipline,
                        date,
                        location,
                        userAccountDTO.id,
                        "",
                        ""
                    ))
                    finish()
                }
            } else
                Toast.makeText(this@PublishChallengePostForm, "Error: No se pudo obtener la cuenta de usuario", Toast.LENGTH_SHORT).show()
        }
    }
}