package com.appmovil24.starproyect.model

data class UserAccountDTO(
    val id : String="",
    val surname: String = "",
    val email: String = "",
    val isSupervisor: Boolean = false,
    val name: String = "",
    val challengesAmount: Int =0,
    val accumulatedScore: Int =0,
    var profileImage: String = ""
)