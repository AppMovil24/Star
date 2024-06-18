package com.appmovil24.starproyect.Model

import com.google.firebase.firestore.GeoPoint
import java.util.Date

public data class Competencia(
    val createDateTime: Date? = null,
    val disciplina: String? = null,
    val estado: String? = null,
    val fecha: Date? = null,
    val fiscalizada: Boolean? = null,
    val tipo: String? = null,
    val ubicacion: GeoPoint? = null,
    val userAcept: String? = null,
    val userCreate: String? = null,
    val userFiscal: String? = null
)