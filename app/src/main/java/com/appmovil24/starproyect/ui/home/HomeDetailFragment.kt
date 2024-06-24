package com.appmovil24.starproyect.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.appmovil24.starproyect.model.ChallengePostDTO
import com.appmovil24.starproyect.R
import com.appmovil24.starproyect.databinding.FragmentHomeDetailBinding
import com.appmovil24.starproyect.model.ChallengePost
import com.appmovil24.starproyect.model.UserAccountDTO
import com.appmovil24.starproyect.repository.ChallengePostRepository
import com.appmovil24.starproyect.repository.UserAccountRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


/**
 * A fragment representing a single competencia detail screen.
 * This fragment is either contained in a [HomeListFragment]
 * in two-pane mode (on larger screen devices) or self-contained
 * on handsets.
 */
class HomeDetailFragment : Fragment() {

    private var binding: FragmentHomeDetailBinding? = null
    private lateinit var challengePost : ChallengePost
    private lateinit var userAccountRepository: UserAccountRepository
    private lateinit var challengePostRepository: ChallengePostRepository
    private lateinit var publisherImageView: ImageView
    private lateinit var opponentImageView: ImageView
    private lateinit var supervisorImageView: ImageView
    private lateinit var view : View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userAccountRepository = UserAccountRepository(FirebaseAuth.getInstance().currentUser!!)
        challengePostRepository = ChallengePostRepository()
        arguments?.let {
            if (it.containsKey(ARG_CHALLENGE_POST)) {
                val gson = Gson()
                val challengePostJson = arguments?.getString(ARG_CHALLENGE_POST)
                challengePost = gson.fromJson(challengePostJson, ChallengePost::class.java)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeDetailBinding.inflate(inflater, container, false)
        val rootView = binding?.root

        updateContent()

        return rootView
    }

    override fun onResume() {
        super.onResume()
        challengePostRepository.get(challengePost.id) { challengePostDTO: ChallengePostDTO? ->
            challengePost.schedule = challengePostDTO?.schedule
            challengePost.date = challengePostDTO?.date
            challengePost.discipline = challengePostDTO?.discipline
            challengePost.location = challengePostDTO?.location
            updateContent()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        publisherImageView = view.findViewById<ImageView>(R.id.home_detail_publisher_image)
        opponentImageView = view.findViewById<ImageView>(R.id.home_detail_opponent_image)
        supervisorImageView = view.findViewById<ImageView>(R.id.home_detail_supervisor_image)
        this.view = view
    }

    private fun updateContent() {
        challengePost?.let { item ->
            binding?.homeDetailDiscipline?.text = "${item.discipline}"
            binding?.homeDetailDate?.text = "${item.date}"
            binding?.homeDetailSchedule?.text = "${item.schedule}"
            userAccountRepository = UserAccountRepository(FirebaseAuth.getInstance().currentUser!!)
            userAccountRepository.get { userAccountDTO: UserAccountDTO? ->
                val currentUserIsPublisher = item.publishBy.equals(userAccountDTO?.id)
                val currentUserIsOpponent = item.acceptedBy.equals(userAccountDTO?.id)
                val currentUserIsSupervisor = item.supervisedBy.equals(userAccountDTO?.id)
                if (currentUserIsPublisher) {
                    binding?.homeDetailEditButton?.visibility = View.VISIBLE
                    binding?.homeDetailEditButton?.setOnClickListener {
                        val intent = Intent(view.context, PublishChallengePostForm::class.java)
                        val bundle = Bundle()
                        bundle.putString("challengePostID", item.id)
                        intent.putExtras(bundle)
                        startActivity(intent)
                    }
                    binding?.homeDetailDeleteButton?.visibility = View.VISIBLE
                    binding?.homeDetailDeleteButton?.setOnClickListener {
                        GlobalScope.launch(Dispatchers.Main) {
                            val result = challengePostRepository.delete(challengePost.id).await()

                            if (result) {
                                findNavController().popBackStack()
                            } else {
                                Toast.makeText(
                                    view.context,
                                    "No se pudo eliminar la publicacion.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
                if (!item.acceptedBy.isNullOrEmpty())
                    userAccountRepository.get(item.acceptedBy) { opponent: UserAccountDTO? ->
                        binding?.homeDetailOpponentFullName?.text =
                            "${opponent?.name} ${opponent?.surname}"
                        binding?.homeDetailOpponentScore?.text = "${opponent?.accumulatedScore}"
                        Picasso.get().load(opponent?.profileImage)
                            .into(binding?.homeDetailOpponentImage)
                    }
                else {
                    binding?.homeDetailOpponentFullName?.visibility = View.GONE
                    binding?.homeDetailOpponentScore?.visibility = View.GONE
                    binding?.homeDetailOpponentImage?.visibility = View.GONE
                    if (!currentUserIsPublisher && !currentUserIsSupervisor) {
                        binding?.homeDetailAcceptChallengeButton?.visibility = View.VISIBLE
                        binding?.homeDetailAcceptChallengeButton?.setOnClickListener {
                            var challengePostDTO = ChallengePostDTO(
                                item.state,
                                item.discipline,
                                item.date,
                                item.schedule,
                                item.location,
                                item.publishBy,
                                userAccountDTO?.id,
                                item.supervisedBy
                            )
                            val updateTask =
                                challengePostRepository.update(item.id, challengePostDTO)
                            updateTask.addOnSuccessListener {
                                Toast.makeText(
                                    requireContext(),
                                    "Acción completada.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }.addOnFailureListener { e ->
                                Toast.makeText(
                                    requireContext(),
                                    "No fue posible realizar la operación: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
                if (!item.supervisedBy.isNullOrEmpty())
                    userAccountRepository.get(item.supervisedBy) { supervisor: UserAccountDTO? ->
                        binding?.homeDetailSupervisorFullName?.text =
                            "${supervisor?.name} ${supervisor?.surname}"
                        Picasso.get().load(supervisor?.profileImage)
                            .into(binding?.homeDetailSupervisorImage)
                    }
                else {
                    binding?.homeDetailSupervisorFullName?.visibility = View.GONE
                    binding?.homeDetailSupervisorImage?.visibility = View.GONE
                    if (!currentUserIsPublisher && !currentUserIsOpponent) {
                        binding?.homeDetailSuperviseChallengeButton?.visibility = View.VISIBLE
                        binding?.homeDetailSuperviseChallengeButton?.setOnClickListener {
                            var challengePostDTO = ChallengePostDTO(
                                item.state,
                                item.discipline,
                                item.date,
                                item.schedule,
                                item.location,
                                item.publishBy,
                                item.acceptedBy,
                                userAccountDTO?.id
                            )
                            val updateTask =
                                challengePostRepository.update(item.id, challengePostDTO)
                            updateTask.addOnSuccessListener {
                                Toast.makeText(
                                    requireContext(),
                                    "Acción completada.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }.addOnFailureListener { e ->
                                Toast.makeText(
                                    requireContext(),
                                    "No fue posible realizar la operación: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
                userAccountRepository.get(item.publishBy!!) { publisher: UserAccountDTO? ->
                    binding?.homeDetailPublisherFullName?.text =
                        "${publisher?.name} ${publisher?.surname}"
                    binding?.homeDetailPublisherScore?.text = "${publisher?.accumulatedScore}"
                    Picasso.get().load(publisher?.profileImage)
                        .into(binding?.homeDetailPublisherImage)
                }
            }
        }
    }

    companion object {
        const val ARG_CHALLENGE_POST = ""
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
