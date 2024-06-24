package com.appmovil24.starproyect.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.appmovil24.starproyect.R
import com.appmovil24.starproyect.activity.feed.Feed
import com.appmovil24.starproyect.model.UserAccountDTO
import com.appmovil24.starproyect.repository.UserAccountRepository
import com.appmovil24.starproyect.databinding.ActivityUserProfileBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

import com.google.firebase.auth.FirebaseAuth

class UserProfile : AppCompatActivity() {

    private lateinit var binding : ActivityUserProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val auth = FirebaseAuth.getInstance()
        val userAccountRepository = UserAccountRepository(auth.currentUser!!)

        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userAccountRepository.get { userAccountDTO: UserAccountDTO? ->
            if (userAccountDTO != null)
                displayUserInfo(userAccountDTO)
        }

        // Setup Bottom Navigation
        val navView: BottomNavigationView = binding.navView
        navView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_preferences -> {
                    val intent = Intent(this, Preferences::class.java)
                    startActivity(intent)
                    true

                }
                R.id.navigation_publish -> {
                    val intent = Intent(this, PublishChallengePostForm::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_user_profile -> {
                    // Ya estás en la actividad de userProfile, no hagas nada
                    true
                }
                R.id.navigation_feed -> {
                    val intent = Intent(this, Feed::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
        // Seleccionar el item de preferencias en el BottomNavigationView
        navView.selectedItemId = R.id.navigation_user_profile
    }

    private fun displayUserInfo(userAccountDTO: UserAccountDTO) {
        val name = userAccountDTO.name
        val surname = userAccountDTO.surname
        val totalPoints = userAccountDTO.challengesAmount * userAccountDTO.accumulatedScore

        binding.userNameTextView.text = getString(R.string.concatenate_two_string, name, surname)
        binding.disciplineTextView.text = getString(R.string.discipline_label, userAccountDTO.discipline)
        binding.pointsTextView.text = getString(R.string.score_label, totalPoints.toString())
    }

}