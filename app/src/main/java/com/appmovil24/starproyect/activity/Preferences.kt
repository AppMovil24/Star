package com.appmovil24.starproyect.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.appmovil24.starproyect.R
import com.appmovil24.starproyect.activity.feed.Feed
import com.appmovil24.starproyect.databinding.ActivityFeedBinding
import com.appmovil24.starproyect.databinding.ActivityPreferencesBinding
import com.appmovil24.starproyect.repository.AuthenticationRepository
import com.google.android.material.bottomnavigation.BottomNavigationView

class Preferences : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityPreferencesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.logoutButton.setOnClickListener {
            AuthenticationRepository.signOut(this)
        }

        // Setup Bottom Navigation
        val navView: BottomNavigationView = binding.navView
        navView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_preferences -> {
                    // Ya estás en la actividad de preferencias, no hagas nada
                    true
                }
                R.id.navigation_publish -> {
                    val intent = Intent(this, PublishChallengePostForm::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_user_profile -> {
                    val intent = Intent(this, UserProfile::class.java)
                    startActivity(intent)
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
        navView.selectedItemId = R.id.navigation_preferences
    }
}