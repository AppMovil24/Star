package com.appmovil24.starproyect.ui.home

import android.content.Intent
import android.os.Bundle
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
import com.appmovil24.starproyect.databinding.FragmentHomeListBinding
import com.appmovil24.starproyect.databinding.HomeListContentBinding
import com.appmovil24.starproyect.model.ChallengePost
import com.appmovil24.starproyect.repository.ChallengePostRepository
import com.google.gson.Gson
import kotlinx.coroutines.launch

class HomeListFragment : Fragment() {

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

    private var binding: FragmentHomeListBinding? = null
    private lateinit  var view: View
    private lateinit var challengePostRepository: ChallengePostRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        challengePostRepository = ChallengePostRepository()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeListBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.view = view

        ViewCompat.addOnUnhandledKeyEventListener(view, unhandledKeyEventListenerCompat)
        binding?.publishChallengeButton?.setOnClickListener {
            val intent = Intent(view.context, PublishChallengePostForm::class.java)
            startActivity(intent)
        }
        /*binding?.preferencesButton?.setOnClickListener {
            val intent = Intent(view.context, Preferences::class.java)
            startActivity(intent)
        }
        binding?.userProfileButton?.setOnClickListener {
            val intent = Intent(view.context, UserProfile::class.java)
            startActivity(intent)
        }*/
        onResume()
    }

    override fun onResume() {
        super.onResume()
        val recyclerView: RecyclerView = binding?.homeList !!
        val itemDetailFragmentContainer: View? = view.findViewById(R.id.home_detail_nav_container)
        lifecycleScope.launch {
            recyclerView.adapter = SimpleItemRecyclerViewAdapter(
                challengePostRepository.getAll(), itemDetailFragmentContainer)
        }
    }

    class SimpleItemRecyclerViewAdapter(
        private val values: List<ChallengePost>,
        private val itemDetailFragmentContainer: View?
    ) : RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = HomeListContentBinding.inflate(
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
                    val ChallengePost = itemView.tag as ChallengePost
                    val bundle = Bundle()
                    val gson = Gson()
                    val challengePostJson = gson.toJson(ChallengePost)
                    bundle.putString(HomeDetailFragment.ARG_CHALLENGE_POST, challengePostJson)
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

        inner class ViewHolder(binding: HomeListContentBinding) :
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