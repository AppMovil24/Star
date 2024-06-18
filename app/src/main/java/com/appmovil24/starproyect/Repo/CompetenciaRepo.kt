package com.appmovil24.starproyect.Repo

import com.appmovil24.starproyect.Model.Competencia
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

public class CompetenciaRepo() {

    private val db = Firebase.firestore
    private val usersCollection = db.collection("competencias")

    // Método para obtener todas las competencias como una lista
    suspend fun getAll(): List<Competencia> {
        val competenciasList = mutableListOf<Competencia>()
        try {
            val querySnapshot = usersCollection.get().await()
            for (document in querySnapshot.documents) {
                val competencia = document.toObject(Competencia::class.java)
                competencia?.let {
                    competenciasList.add(it)
                }
            }
        } catch (e: Exception) {
            // Manejar cualquier excepción que pueda ocurrir al obtener los documentos
            e.printStackTrace()
        }
        return competenciasList
    }

}

