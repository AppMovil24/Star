package com.appmovil24.starproyect.ui.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.appmovil24.starproyect.databinding.ActivityChallengeListFilterBinding
import com.appmovil24.starproyect.enum.ChallengePostState
import com.appmovil24.starproyect.repository.ChallengePostRepository

class FilterActitity : AppCompatActivity() {

    private lateinit var binding : ActivityChallengeListFilterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChallengeListFilterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(ChallengePostRepository.states.contains(ChallengePostState.OPEN.name) ||
            ChallengePostRepository.states.isEmpty())
            binding.byOpenedChallenges.isChecked = true
        if(ChallengePostRepository.states.contains(ChallengePostState.ACCEPTED.name) ||
            ChallengePostRepository.states.isEmpty())
            binding.byAcceptedChallenges.isChecked = true
        if(ChallengePostRepository.states.contains(ChallengePostState.COMPLETED.name) ||
            ChallengePostRepository.states.isEmpty())
            binding.byCompletedChallenges.isChecked = true

        binding.applyChallengeListFilters.setOnClickListener {
            ChallengePostRepository.states.clear()
            if(binding.byOpenedChallenges.isChecked)
                ChallengePostRepository.states.add(ChallengePostState.OPEN.name)
            if(binding.byAcceptedChallenges.isChecked)
                ChallengePostRepository.states.add(ChallengePostState.ACCEPTED.name)
            if(binding.byCompletedChallenges.isChecked)
                ChallengePostRepository.states.add(ChallengePostState.COMPLETED.name)
            finish()
        }

    }

}