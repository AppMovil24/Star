package com.appmovil24.starproyect.ui.home

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.appmovil24.starproyect.MyFirebaseMessagingService
import com.appmovil24.starproyect.model.ChallengePostDTO
import com.appmovil24.starproyect.R
import com.appmovil24.starproyect.databinding.FragmentHomeDetailBinding
import com.appmovil24.starproyect.enum.ChallengePostState
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
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import java.util.UUID


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

        refreshDetail()

        return rootView
    }

    override fun onResume() {
        super.onResume()

        refreshDetail()
    }

    private fun refreshDetail() {
        challengePostRepository.get(challengePost.id) { challengePostDTO: ChallengePostDTO? ->
            challengePost.state = challengePostDTO?.state
            challengePost.discipline = challengePostDTO?.discipline
            challengePost.date = challengePostDTO?.date
            challengePost.schedule = challengePostDTO?.schedule
            challengePost.location = challengePostDTO?.location
            challengePost.publishBy = challengePostDTO?.publishBy
            challengePost.acceptedBy = challengePostDTO?.acceptedBy
            challengePost.supervisedBy = challengePostDTO?.supervisedBy
            challengePost.opponentsVote = challengePostDTO?.opponentsVote
            challengePost.publisherVote = challengePostDTO?.publisherVote
            challengePost.supervisorVote = challengePostDTO?.supervisorVote
            challengePost.image = challengePostDTO?.image

            updateContent()
        }
    }

    private fun updateContent() {
        renderChallengeStateSection(challengePost)
        renderGeneralDetailsSection(challengePost)
        userAccountRepository.get { userAccountDTO: UserAccountDTO? ->
            val currentUserIsPublisher = challengePost.publishBy.equals(userAccountDTO?.id)
            val currentUserIsOpponent = challengePost.acceptedBy.equals(userAccountDTO?.id)
            val currentUserIsSupervisor = challengePost.supervisedBy.equals(userAccountDTO?.id)

            renderPublisherSection(currentUserIsPublisher)
            renderOpponentSection(currentUserIsPublisher, currentUserIsSupervisor, userAccountDTO?.id!!)
            renderSupervisorSection(currentUserIsPublisher, currentUserIsOpponent, userAccountDTO.id)

            if(challengePost.state.equals(ChallengePostState.COMPLETED.name))
                renderWinner(challengePost)
            else if(challengePost.state.equals(ChallengePostState.ACCEPTED.name) &&
                ( currentUserIsPublisher || currentUserIsSupervisor || currentUserIsOpponent) )
            {
                initVoteButton(challengePost.publishBy !!, challengePost, binding?.homeDetailPublisherWinsButton !!
                    , currentUserIsSupervisor,currentUserIsPublisher)
                initVoteButton(challengePost.acceptedBy!!, challengePost, binding?.homeDetailOpponentWinsButton !!
                    , currentUserIsSupervisor, currentUserIsPublisher)
            }
        }
    }

    private fun renderPublisherSection(currentUserIsPublisher: Boolean) {
        loadPublisherInformation()
        if (currentUserIsPublisher && challengePost.state.equals(ChallengePostState.OPEN.name)){
            initEditButton(challengePost)
            initDeleteButton(challengePost)
        }
    }

    private fun renderOpponentSection(currentUserIsPublisher: Boolean,
                                      currentUserIsSupervisor: Boolean,
                                      userAccountID: String ) {
        if (hasValue(challengePost.acceptedBy))
            loadOponnent()
        else if (!currentUserIsPublisher && !currentUserIsSupervisor)
            initAcceptChallengeButton(userAccountID)
        else
            binding?.homeDetailOpponentVacancy?.visibility = View.VISIBLE
    }

    private fun renderSupervisorSection(currentUserIsPublisher: Boolean,
                                        currentUserIsOpponent: Boolean,
                                        userAccountID: String ) {
        if (hasValue(challengePost.supervisedBy))
            loadSupervisorInformation()
        else if (!currentUserIsPublisher && !currentUserIsOpponent)
            initSuperviseChallengeButton(userAccountID)
        else
            binding?.homeDetailSupervisorVacancy?.visibility = View.VISIBLE
    }

    private fun renderGeneralDetailsSection(item: ChallengePost) {
        binding?.homeDetailDiscipline?.text = "${item.discipline}"
        binding?.homeDetailDate?.text = "${item.date}"
        binding?.homeDetailSchedule?.text = "${item.schedule}"
        renderMap()
    }

    private fun renderMap() {
        var mapView = binding?.challengePostLocation
        mapView?.setTileSource(TileSourceFactory.MAPNIK)
        mapView?.setMultiTouchControls(true)
        var mapController = mapView?.controller
        mapController?.setZoom(15.0)
        val point = GeoPoint(challengePost.location?.latitude!!, challengePost.location?.longitude!!)
        mapController?.setCenter(point)
        mapView?.overlays?.clear()
        val marker = Marker(mapView)
        marker.position = point
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = "@string/challenge_marker_title"
        mapView?.overlays?.add(marker)
        mapView?.invalidate()
    }

    private fun loadPublisherInformation() {
        initUserFields(challengePost.publishBy !!, challengePost, binding?.homeDetailPublisherFullName !!,
            binding?.homeDetailPublisherImage !!)
    }
    private fun loadOponnent() {
        binding?.homeDetailOpponentFullName?.visibility = View.VISIBLE
        binding?.homeDetailOpponentScore?.visibility = View.VISIBLE
        binding?.homeDetailOpponentImage?.visibility = View.VISIBLE
        initUserFields(
            challengePost.acceptedBy!!, challengePost, binding?.homeDetailOpponentFullName!!,
            binding?.homeDetailOpponentImage!!
        )
    }

    private fun initAcceptChallengeButton(userAccountID : String) {
        var challengePostDTO = createDTO(challengePost)
        challengePostDTO.state = ChallengePostState.ACCEPTED.name // Si presiona el boton.
        challengePostDTO.acceptedBy = userAccountID
        initUpdateButton(challengePost.id, challengePostDTO, binding?.homeDetailAcceptChallengeButton !!, binding?.homeDetailOpponentImage!!)
    }

    private fun loadSupervisorInformation() {
        binding?.homeDetailSupervisorFullName?.visibility = View.VISIBLE
        binding?.homeDetailSupervisorImage?.visibility = View.VISIBLE
        initUserFields(challengePost.supervisedBy !!, challengePost, binding?.homeDetailSupervisorFullName !!,
            binding?.homeDetailSupervisorImage !!)
    }

    private fun initSuperviseChallengeButton(userAccountID: String) {
        var challengePostDTO = createDTO(challengePost)
        challengePostDTO.supervisedBy = userAccountID
        initUpdateButton(challengePost.id, challengePostDTO, binding?.homeDetailSuperviseChallengeButton !!, binding?.homeDetailSupervisorImage!!)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        publisherImageView = view.findViewById<ImageView>(R.id.home_detail_publisher_image)
        opponentImageView = view.findViewById<ImageView>(R.id.home_detail_opponent_image)
        supervisorImageView = view.findViewById<ImageView>(R.id.home_detail_supervisor_image)
        this.view = view
    }

    private fun renderChallengeStateSection(item : ChallengePost) {
        if(item.state.equals(ChallengePostState.ACCEPTED.name))
            renderVotes(item)
        else if(item.state.equals(ChallengePostState.COMPLETED.name))
            renderWinner(item)
    }

    private fun renderVotes(item : ChallengePost) {
        if (!item.publisherVote.isNullOrEmpty()) {
            binding?.homeDetailVotes?.visibility = View.VISIBLE
            binding?.homeDetailPublishersVote?.visibility = View.VISIBLE
            binding?.homeDetailPublishersVote?.text = getString(
                R.string.winner_by, item.publishBy, item.publisherVote
            )
        }
        if (!item.opponentsVote.isNullOrEmpty()) {
            binding?.homeDetailVotes?.visibility = View.VISIBLE
            binding?.homeDetailOpponentsVote?.visibility = View.VISIBLE
            binding?.homeDetailOpponentsVote?.text = getString(
                R.string.winner_by, item.acceptedBy, item.opponentsVote
            )
        }
        if (!item.supervisorVote.isNullOrEmpty()) {
            binding?.homeDetailVotes?.visibility = View.VISIBLE
            binding?.homeDetailSupervisorsVote?.visibility = View.VISIBLE
            binding?.homeDetailSupervisorsVote?.text = getString(
                R.string.winner_by, item.supervisedBy, item.supervisorVote
            )
        }
    }

    private fun renderWinner(item : ChallengePost) {
        binding?.homeDetailVotes?.visibility = View.VISIBLE
        binding?.homeDetailWinnerContainer?.visibility = View.VISIBLE
        binding?.homeDetailPublisherWinsButton?.visibility = View.GONE
        binding?.homeDetailOpponentWinsButton?.visibility = View.GONE
        binding?.homeDetailPublishersVote?.visibility = View.GONE
        binding?.homeDetailOpponentsVote?.visibility = View.GONE
        var winner = item.supervisorVote
        if(! hasValue(winner))
            winner = item.publishBy
        userAccountRepository.get(winner!!) { user: UserAccountDTO? ->
            Picasso.get().load(user?.profileImage).into(binding?.homeDetailWinnerImage)
            binding?.homeDetailWinnerUsername?.text = "${user?.name} ${user?.surname}"
        }
    }

    private fun initEditButton(item: ChallengePost) {
        binding?.homeDetailEditButton?.visibility = View.VISIBLE
        binding?.homeDetailEditButton?.setOnClickListener {
            val intent = Intent(view.context, PublishChallengePostFormActivity::class.java)
            val bundle = Bundle()
            bundle.putString("challengePostID", item.id)
            intent.putExtras(bundle)
            startActivity(intent)
        }
    }

    private fun initDeleteButton(item: ChallengePost) {
        binding?.homeDetailDeleteButton?.visibility = View.VISIBLE
        binding?.homeDetailDeleteButton?.setOnClickListener {
            val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
            builder.setTitle("")
                .setMessage("¿ Seguro desea guardar esta competencia ?")
                .setPositiveButton(getString(R.string.yes), DialogInterface.OnClickListener { dialog, which ->
                    GlobalScope.launch(Dispatchers.Main) {
                        val result = challengePostRepository.delete(challengePost.id).await()
                        if (result) {
                            findNavController().popBackStack()
                        } else {
                            Toast.makeText(view.context, "No se pudo eliminar la publicacion.",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
                })
                .setNegativeButton(
                    getString(R.string.no),
                    DialogInterface.OnClickListener { dialog, which -> // Do something when Cancel is clicked
                        dialog.dismiss()
                    })
                .show()
        }
    }

    private fun initUserFields(userName: String, item: ChallengePost, fullNameField: TextView,
                               imageField: ImageView) {
        userAccountRepository.get(userName) { user: UserAccountDTO? ->
            fullNameField.visibility = View.VISIBLE
            imageField.visibility = View.VISIBLE
            fullNameField.text = "${user?.name} ${user?.surname} ( ${userName} )"
            Picasso.get().load(user?.profileImage).into(imageField)
        }
    }

    private fun initUpdateButton(challengePostID: String, challengePostDTO: ChallengePostDTO,
                                 acceptRolButton: Button, imageView: ImageView? ) {
        acceptRolButton.visibility = View.VISIBLE
        acceptRolButton.setOnClickListener {
            val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
            builder.setTitle("")
                .setMessage("¿ Seguro desea guardar esta competencia ?")
                .setPositiveButton(getString(R.string.yes), DialogInterface.OnClickListener { dialog, which ->
                    val updateTask = challengePostRepository.update(challengePostID, challengePostDTO)
                    updateTask.addOnSuccessListener {
                        acceptRolButton.visibility = View.GONE
                        refreshDetail()
                        Toast.makeText(requireContext(), "Acción completada.", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Error al guardar.", Toast.LENGTH_SHORT).show()
                    }
                })
                .setNegativeButton(
                    getString(R.string.no),
                    DialogInterface.OnClickListener { dialog, which -> // Do something when Cancel is clicked
                        dialog.dismiss()
                    })
                .show()
        }
    }

    private fun createDTO(item: ChallengePost) : ChallengePostDTO {
        return ChallengePostDTO(
            item.state,
            item.discipline,
            item.date,
            item.schedule,
            item.location,
            item.publishBy,
            item.acceptedBy,
            item.supervisedBy,
            item.opponentsVote,
            item.publisherVote,
            item.supervisorVote,
            item.image
        )
    }

    private fun addUserScore(userID: String) {
        userAccountRepository.get(userID) { accountDTO: UserAccountDTO? ->
            accountDTO?.accumulatedScore = accountDTO?.accumulatedScore!! + 1
            userAccountRepository.update(accountDTO) { success ->
                if (success) {
                   /* Toast.makeText(
                        view.context,
                        "Ganador: ${accountDTO.id}",
                        Toast.LENGTH_SHORT
                    ).show()*/
                } else {
                    Log.e("RegisterActivity", "Error al registrar los puntos al usuario")
                }
            }
        }
    }

    private fun initVoteButton(votedUserID: String, item: ChallengePost, button: Button,
                               currentUserIsSupervisor: Boolean, currentUserIsPublisher: Boolean) {
        var challengePostDTO = createDTO(item)
        if(currentUserIsSupervisor) {
            challengePostDTO.supervisorVote = votedUserID
            challengePostDTO.state = ChallengePostState.COMPLETED.name
            addUserScore(challengePostDTO.supervisorVote!!)
        } else if(currentUserIsPublisher)
            challengePostDTO.publisherVote = votedUserID
        else
            challengePostDTO.opponentsVote = votedUserID

        if( challengePostDTO.supervisedBy.isNullOrEmpty() &&
            ! challengePostDTO.publisherVote.isNullOrEmpty() &&
            ! challengePostDTO.opponentsVote.isNullOrEmpty() &&
            challengePostDTO.publisherVote.equals(challengePostDTO.opponentsVote) &&
            ! challengePostDTO.state.equals(ChallengePostState.COMPLETED.name))
        {
            challengePostDTO.state = ChallengePostState.COMPLETED.name
            addUserScore(challengePostDTO.publisherVote!!)
        }

        initUpdateButton(item.id, challengePostDTO, button, null)
    }

    private fun hasValue(text: String?) : Boolean {
        return text != null && text.isNotBlank()
    }

    companion object {
        const val ARG_CHALLENGE_POST = ""
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
