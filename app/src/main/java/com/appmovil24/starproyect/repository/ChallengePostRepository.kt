package com.appmovil24.starproyect.repository

import com.appmovil24.starproyect.model.ChallengePostDTO
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

public class ChallengePostRepository() {

    private val challengePostsCollection = Firebase.firestore.collection("competencias")

    suspend fun getAll(): List<ChallengePostDTO> {
        val competenciasList = mutableListOf<ChallengePostDTO>()
        try {
            val querySnapshot = challengePostsCollection.get().await()
            for (document in querySnapshot.documents) {
                val challengePostDTO = document.toObject(ChallengePostDTO::class.java)
                challengePostDTO?.let {
                    competenciasList.add(it)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return competenciasList
    }

    suspend fun add(challengePostDTO: ChallengePostDTO): Result<String?> {
        return try {
            val documentReference = challengePostsCollection.add(challengePostDTO).await()
            val generatedId = documentReference.id
            Result.success(generatedId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}

