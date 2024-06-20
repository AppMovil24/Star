package com.appmovil24.starproyect.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.appmovil24.starproyect.R
import com.appmovil24.starproyect.model.UserAccountDTO
import com.appmovil24.starproyect.repository.UserAccountRepository
import com.appmovil24.starproyect.databinding.ActivityUserProfileBinding
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
    }

    private fun displayUserInfo(userAccountDTO: UserAccountDTO) {
        val name = getString(R.string.name)
        val surname = getString(R.string.surname)
        val totalPoints = userAccountDTO.challengesAmount * userAccountDTO.accumulatedScore

        binding.userNameTextView.text = getString(R.string.concatenate_two_string, name, surname)
        binding.disciplineTextView.text = getString(R.string.discipline_label, userAccountDTO.discipline)
        binding.pointsTextView.text = getString(R.string.score_label, totalPoints.toString())
    }

}