package com.appmovil24.starproyect.model

import com.google.firebase.firestore.GeoPoint

public data class ChallengePostDTO(
    val state: String? = null,
    val discipline: String? = null,
    val date: String? = null,
    val location: GeoPoint? = null,
    val publishBy: String? = null,
    val acceptedBy: String? = null,
    val supervisedBy: String? = null
)