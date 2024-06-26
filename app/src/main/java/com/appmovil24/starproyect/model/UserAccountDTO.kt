package com.appmovil24.starproyect.model

data class UserAccountDTO(
    var id : String="",
    var surname: String = "",
    var email: String = "",
    var isSupervisor: Boolean = false,
    var name: String = "",
    var challengesAmount: Int =0,
    var accumulatedScore: Int =0,
    var profileImage: String = ""
)