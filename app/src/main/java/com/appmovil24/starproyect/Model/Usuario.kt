package com.appmovil24.starproyect.Model

data class Usuario(
    val usuarioId : String="",
    val apellido: String = "",
    val competidor: Boolean = false,
    val email: String = "",
    val fiscalizador: Boolean = false,
    val nombre: String = "",
    val disciplina: String= "",
    val competencias: Int =0,
    val puntos: Int =0
)