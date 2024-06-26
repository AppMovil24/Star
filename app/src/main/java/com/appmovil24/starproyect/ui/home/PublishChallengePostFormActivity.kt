package com.appmovil24.starproyect.ui.home
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.view.MotionEvent
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.appmovil24.starproyect.databinding.ActivityPublishChallengePostBinding
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint as OsmdroidGeoPoint
import com.google.firebase.firestore.GeoPoint as FirestoreGeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import androidx.lifecycle.lifecycleScope
import com.appmovil24.starproyect.enum.ChallengePostState
import com.appmovil24.starproyect.model.ChallengePostDTO
import com.appmovil24.starproyect.repository.ChallengePostRepository
import com.appmovil24.starproyect.repository.UserAccountRepository
import com.google.firebase.auth.FirebaseAuth
import org.osmdroid.api.IGeoPoint
import org.osmdroid.api.IMapController
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay


class PublishChallengePostFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPublishChallengePostBinding
    private lateinit var userAccountRepository: UserAccountRepository
    private lateinit var challengePostRepository: ChallengePostRepository
    private lateinit var mapView: MapView
    private lateinit var mapController: IMapController
    private var challengePostID: String? = null
    private var markerLocation: FirestoreGeoPoint? = null
    private var defaultLatitude: Double = 0.0
    private var defaultLongitude: Double = 0.0

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userAccountRepository = UserAccountRepository(FirebaseAuth.getInstance().currentUser!!)
        challengePostRepository = ChallengePostRepository()

        binding = ActivityPublishChallengePostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val disciplines = listOf("Tenis", "Ajedrez", "Boxeo")
        val disciplinesAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, disciplines)
        disciplinesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.inputDiscipline.adapter = disciplinesAdapter

        defaultLatitude = -34.7632
        defaultLongitude = -58.2146
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            } else {
                initMap(defaultLatitude, defaultLongitude)
                getCurrentLocation()
            }
        } else {
            initMap(defaultLatitude, defaultLongitude)
            getCurrentLocation()
        }

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
                initMap(challengePostDTO?.location?.latitude!!, challengePostDTO.location?.longitude!!)
            }
        }


        binding.publishChallengePostButton.setOnClickListener {
            lifecycleScope.launch {
                publishCompetition()
            }
        }
    }

    private fun initMap(latitude: Double, longitude: Double) {
        Configuration.getInstance().load(
            applicationContext,
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
        )
        mapView = binding.mapView
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapController = mapView.controller
        mapController.setZoom(15.0)
        setMapClickListener()
        val berazateguiCenter = GeoPoint(latitude, longitude)
        mapController.setCenter(berazateguiCenter)
        val marker = Marker(mapView)
        marker.position = berazateguiCenter
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = "Lugar de la competencia"
        mapView.overlays?.add(marker)
        mapView.invalidate()
    }

    fun convertToFirestoreGeoPoint(osmdroidGeoPoint: IGeoPoint): FirestoreGeoPoint {
        return FirestoreGeoPoint(osmdroidGeoPoint.latitude, osmdroidGeoPoint.longitude)
    }

    fun convertToOsmdroidGeoPoint(firestoreGeoPoint: FirestoreGeoPoint): OsmdroidGeoPoint {
        return OsmdroidGeoPoint(firestoreGeoPoint.latitude, firestoreGeoPoint.longitude)
    }

    private fun getCurrentLocation(): FirestoreGeoPoint {
        val locationProvider = GpsMyLocationProvider(applicationContext)
        val location = locationProvider.lastKnownLocation
        return if (location != null) {
            convertToFirestoreGeoPoint(OsmdroidGeoPoint(location.latitude, location.longitude))
        } else {
            convertToFirestoreGeoPoint(OsmdroidGeoPoint(-34.7632, -58.2146))
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initMap(defaultLatitude, defaultLongitude)
            } else {
                // Handle denied permission
            }
        }
    }
    private fun setMapClickListener() {
        mapView.overlays.add(object : Overlay() {
            override fun onSingleTapConfirmed(e: MotionEvent, mapView: MapView): Boolean {
                val projection = mapView.projection
                val geoPoint = projection.fromPixels(e.x.toInt(), e.y.toInt())

                markerLocation = convertToFirestoreGeoPoint(geoPoint)

                mapView?.overlays?.clear()

                val marker = Marker(mapView)
                marker.position = geoPoint as GeoPoint?
                mapView.overlays.add(marker)
                mapView.invalidate()
                setMapClickListener()
                return super.onSingleTapConfirmed(e, mapView)
            }
        })
    }
    suspend fun publishCompetition() {
        val date = binding.inputDate.text.toString().trim()
        val discipline = binding.inputDiscipline.selectedItem as String
        val schedule = binding.inputSchedule.text.toString().trim()
        val location = markerLocation

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
                            Toast.makeText(this@PublishChallengePostFormActivity, "Acción completada.", Toast.LENGTH_SHORT).show()
                            finish()
                        }.addOnFailureListener { e ->
                            ok = false
                            Toast.makeText(this@PublishChallengePostFormActivity, "No fue posible realizar la operación: ${e.message}", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }

                }
            } else
                Toast.makeText(this@PublishChallengePostFormActivity, "Error: No se pudo obtener la cuenta de usuario", Toast.LENGTH_SHORT).show()
        }
    }
}