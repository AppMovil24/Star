package com.appmovil24.starproyect.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.appmovil24.starproyect.databinding.ActivityFeedBinding
import com.appmovil24.starproyect.databinding.ActivityPreferencesBinding
import com.appmovil24.starproyect.repository.AuthenticationRepository

class Preferences  : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityPreferencesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.logoutButton.setOnClickListener {
            AuthenticationRepository.signOut(this)
        }
    }

}