package com.appmovil24.starproyect.activity.feed

import android.content.ClipData
import android.os.Bundle
import android.util.Log
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.firebase.database.*
import com.appmovil24.starproyect.databinding.FragmentCompetenciaDetailBinding
import com.appmovil24.starproyect.model.ChallengePostDTO
import com.appmovil24.starproyect.R
import com.google.firebase.firestore.GeoPoint
import com.google.gson.Gson


/**
 * A fragment representing a single competencia detail screen.
 * This fragment is either contained in a [competenciaListFragment]
 * in two-pane mode (on larger screen devices) or self-contained
 * on handsets.
 */
    class competenciaDetailFragment : Fragment() {

    private var binding: FragmentCompetenciaDetailBinding? = null
    private lateinit var challengePostDTO : ChallengePostDTO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            if (it.containsKey(ARG_CHALLENGE_POST_DTO)) {
                val gson = Gson()
                val challengePostJson = arguments?.getString(ARG_CHALLENGE_POST_DTO)
                challengePostDTO = gson.fromJson(challengePostJson, ChallengePostDTO::class.java)
                /*if (challengePostDTO != null) {
                    competitionRef = database.getReference("competitions").child(challengePostDTO)
                    fetchCompetitionDetail(challengePostDTO)
                }*/
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCompetenciaDetailBinding.inflate(inflater, container, false)
        val rootView = binding?.root

        updateContent()

        return rootView
    }

    private fun updateContent() {
        challengePostDTO?.let { item ->
            binding?.competenciaDetail?.text = "${item.state}"
        } ?: run {
            binding?.competenciaDetail?.text = requireContext().getString(R.string.loading)
        }
    }

    companion object {
        const val ARG_CHALLENGE_POST_DTO = ""
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
