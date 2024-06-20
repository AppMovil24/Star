package com.appmovil24.starproyect.activity.feed

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.appmovil24.starproyect.R
import com.appmovil24.starproyect.databinding.FragmentCompetenciaListBinding
import com.appmovil24.starproyect.databinding.CompetenciaListContentBinding
import com.appmovil24.starproyect.model.ChallengePostDTO
import com.appmovil24.starproyect.repository.ChallengePostRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import kotlinx.coroutines.launch

class competenciaListFragment : Fragment() {

    private val unhandledKeyEventListenerCompat =
        ViewCompat.OnUnhandledKeyEventListenerCompat { v, event ->
            if (event.keyCode == KeyEvent.KEYCODE_Z && event.isCtrlPressed) {
                Toast.makeText(
                    v.context,
                    "Undo (Ctrl + Z) shortcut triggered",
                    Toast.LENGTH_LONG
                ).show()
                true
            } else if (event.keyCode == KeyEvent.KEYCODE_F && event.isCtrlPressed) {
                Toast.makeText(
                    v.context,
                    "Find (Ctrl + F) shortcut triggered",
                    Toast.LENGTH_LONG
                ).show()
                true
            }
            false
        }

    private var binding: FragmentCompetenciaListBinding? = null
    private lateinit var challengePostRepository: ChallengePostRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        challengePostRepository = ChallengePostRepository()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCompetenciaListBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView: RecyclerView = binding?.competenciaList !!
        val itemDetailFragmentContainer: View? = view.findViewById(R.id.competencia_detail_nav_container)

        ViewCompat.addOnUnhandledKeyEventListener(view, unhandledKeyEventListenerCompat)
        lifecycleScope.launch {
            recyclerView.adapter = SimpleItemRecyclerViewAdapter(
                challengePostRepository.getAll(), itemDetailFragmentContainer)
        }
    }

    class SimpleItemRecyclerViewAdapter(
        private val values: List<ChallengePostDTO>,
        private val itemDetailFragmentContainer: View?
    ) : RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = CompetenciaListContentBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = values[position]
            holder.idView.text = holder.itemView.context.getString(
                R.string.concatenate_two_string, item.discipline, item.date.toString())
//            holder.contentView.text = ""

            with(holder.itemView) {
                tag = item
                setOnClickListener { itemView ->
                    val challengePostDTO = itemView.tag as ChallengePostDTO
                    val bundle = Bundle()
                    val gson = Gson()
                    val challengePostJson = gson.toJson(challengePostDTO)
                    bundle.putString(competenciaDetailFragment.ARG_CHALLENGE_POST_DTO, challengePostJson)
                    if (itemDetailFragmentContainer != null) {
                        itemDetailFragmentContainer.findNavController()
                            .navigate(R.id.fragment_competencia_detail, bundle)
                    } else {
                        itemView.findNavController().navigate(R.id.show_competencia_detail, bundle)
                    }
                }
            }
        }

        override fun getItemCount() = values.size

        inner class ViewHolder(binding: CompetenciaListContentBinding) :
            RecyclerView.ViewHolder(binding.root) {
            val idView: TextView = binding.idText
            val contentView: TextView = binding.content
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}