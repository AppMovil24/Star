package com.appmovil24.starproyect.model

import com.google.firebase.firestore.GeoPoint
import java.io.Serializable

public data class ChallengePost(
    var id: String,
    var state: String? = null,
    var discipline: String? = null,
    var date: String? = null,
    var schedule: String? = null,
    var location: GeoPoint? = null,
    var publishBy: String? = null,
    var acceptedBy: String? = null,
    var supervisedBy: String? = null,
    var opponentsVote: String? = null,
    var publisherVote: String? = null,
    var supervisorVote: String? = null,
    var image: String? = null
)

public data class ChallengePostDTO(
    var state: String? = null,
    var discipline: String? = null,
    var date: String? = null,
    var schedule: String? = null,
    var location: GeoPoint? = null,
    var publishBy: String? = null,
    var acceptedBy: String? = null,
    var supervisedBy: String? = null,
    var opponentsVote: String? = null,
    var publisherVote: String? = null,
    var supervisorVote: String? = null,
    var image: String? = null
)
