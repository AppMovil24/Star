package com.appmovil24.starproyect.model

import com.google.firebase.firestore.GeoPoint
import java.io.Serializable

public data class ChallengePost(
    val id: String,
    val state: String? = null,
    var discipline: String? = null,
    var date: String? = null,
    var schedule: String? = null,
    var location: GeoPoint? = null,
    val publishBy: String? = null,
    val acceptedBy: String? = null,
    val supervisedBy: String? = null
)

public data class ChallengePostDTO(
    val state: String? = null,
    val discipline: String? = null,
    val date: String? = null,
    val schedule: String? = null,
    val location: GeoPoint? = null,
    val publishBy: String? = null,
    val acceptedBy: String? = null,
    val supervisedBy: String? = null
)
