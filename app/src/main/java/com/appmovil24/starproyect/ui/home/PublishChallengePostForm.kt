package com.appmovil24.starproyect.ui.home

import android.os.Bundle
import android.text.Editable
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.appmovil24.starproyect.R
import com.appmovil24.starproyect.databinding.ActivityPublishChallengePostBinding
import com.appmovil24.starproyect.enum.ChallengePostState
import com.appmovil24.starproyect.model.ChallengePostDTO
import com.appmovil24.starproyect.repository.ChallengePostRepository
import com.appmovil24.starproyect.repository.UserAccountRepository
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.launch

class PublishChallengePostForm : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding : ActivityPublishChallengePostBinding
    private lateinit var userAccountRepository : UserAccountRepository
    private lateinit var challengePostRepository : ChallengePostRepository
    private var challengePostID : String? = null
    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userAccountRepository = UserAccountRepository(FirebaseAuth.getInstance().currentUser!!)
        challengePostRepository = ChallengePostRepository()

        binding = ActivityPublishChallengePostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val disciplines = listOf("Discipline 1", "Discipline 2", "Discipline 3")
        val disciplinesAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, disciplines)
        disciplinesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.inputDiscipline.adapter = disciplinesAdapter

        val bundle: Bundle? = intent.extras
        if (bundle != null) {
            challengePostID = bundle.getString("challengePostID")
            challengePostRepository.get(challengePostID!!) { challengePostDTO: ChallengePostDTO? ->
                val scheduleText: Editable? = challengePostDTO?.schedule?.let {
                    Editable.Factory.getInstance().newEditable(it)
                }
                binding.inputSchedule.text = scheduleText
                val dateText: Editable? = challengePostDTO?.date?.let {
                    Editable.Factory.getInstance().newEditable(it)
                }
                binding.inputDate.text = dateText
                val position = disciplines.indexOf(challengePostDTO?.discipline)
                if (position != -1)
                    binding.inputDiscipline.setSelection(position)
            }
        }

        binding.publishChallengePostButton.setOnClickListener {
            lifecycleScope.launch {
                publishCompetition()
            }
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.challenge_location_map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marcador en Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    suspend fun publishCompetition() {
        val date = binding.inputDate.text.toString().trim()
        val discipline = binding.inputDiscipline.selectedItem as String
        val schedule = binding.inputSchedule.text.toString().trim()
        val location = GeoPoint(37.7749, -122.4194); // hay que obtener de un mapa de google seleccionado por el usuario

        if (date.isEmpty() || schedule.isEmpty() || discipline.isEmpty() || location == null) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        userAccountRepository.get { userAccountDTO ->
            if (userAccountDTO != null) {
                lifecycleScope.launch {
                    var challengePostDTO = ChallengePostDTO(
                        ChallengePostState.OPEN.name,
                        discipline,
                        date,
                        schedule,
                        location,
                        userAccountDTO.id,
                        "",
                        ""
                    )
                    var ok : Boolean = false
                    if (challengePostID == null) {
                        val result = challengePostRepository.add(challengePostDTO)
                        ok = result.isSuccess
                        finish()
                    } else {
                        val updateTask = challengePostRepository.update(challengePostID!!, challengePostDTO)
                        updateTask.addOnSuccessListener {
                            ok = true
                            Toast.makeText(this@PublishChallengePostForm, "Acción completada.", Toast.LENGTH_SHORT).show()
                            finish()
                        }.addOnFailureListener { e ->
                            ok = false
                            Toast.makeText(this@PublishChallengePostForm, "No fue posible realizar la operación: ${e.message}", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }

                }
            } else
                Toast.makeText(this@PublishChallengePostForm, "Error: No se pudo obtener la cuenta de usuario", Toast.LENGTH_SHORT).show()
        }
    }
}