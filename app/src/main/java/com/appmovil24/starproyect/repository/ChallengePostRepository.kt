package com.appmovil24.starproyect.repository

import com.appmovil24.starproyect.enum.ChallengePostState
import com.appmovil24.starproyect.model.ChallengePost
import com.appmovil24.starproyect.model.ChallengePostDTO
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

public class ChallengePostRepository() {

    companion object {
        var states: MutableList<String> = mutableListOf()
    }

    private val challengePostsCollection = Firebase.firestore.collection("competencias")

    fun get(challengePostID: String, onComplete: (ChallengePostDTO?) -> Unit) {
        val userRef = challengePostsCollection.document(challengePostID)

        userRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val challengePostDTO = documentSnapshot.toObject(ChallengePostDTO::class.java)
                    onComplete(challengePostDTO)
                } else {
                    onComplete(null)
                }
            }
            .addOnFailureListener { exception ->
                onComplete(null)
            }
    }

    suspend fun getAll(): List<ChallengePost> {
        val competenciasList = mutableListOf<ChallengePost>()
        try {
            val querySnapshot : QuerySnapshot
            if(ChallengePostRepository.states.isEmpty())
                querySnapshot = challengePostsCollection.get().await()
            else
                querySnapshot = challengePostsCollection.whereIn("state", ChallengePostRepository.states).get().await()

            for (document in querySnapshot.documents) {
                val challengePostDTO = document.toObject(ChallengePostDTO::class.java)
                challengePostDTO?.let {
                    competenciasList.add(ChallengePost(
                        id = document.id,
                        state = document.getString("state"),
                        discipline = document.getString("discipline"),
                        date = document.getString("date"),
                        schedule = document.getString("schedule"),
                        location = document.getGeoPoint("location"),
                        publishBy = document.getString("publishBy"),
                        acceptedBy = document.getString("acceptedBy"),
                        supervisedBy = document.getString("supervisedBy"),
                        opponentsVote = document.getString("opponentsVote"),
                        publisherVote = document.getString("publisherVote"),
                        supervisorVote = document.getString("supervisorVote"),
                        image = document.getString("image"),
                    ))
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

    fun update(challengePostID: String, challengePostDTO: ChallengePostDTO): Task<Boolean> {
        val completionSource = TaskCompletionSource<Boolean>()
        val documentRef = challengePostsCollection.document(challengePostID)

        documentRef.set(challengePostDTO, SetOptions.merge())
            .addOnSuccessListener {
                completionSource.setResult(true)
            }
            .addOnFailureListener { e ->
                completionSource.setResult(false)
            }

        return completionSource.task
    }

    fun delete(challengePostID: String): Task<Boolean> {
        val completionSource = TaskCompletionSource<Boolean>()
        val documentRef = challengePostsCollection.document(challengePostID)

        documentRef.delete()
            .addOnSuccessListener {
                completionSource.setResult(true)
            }
            .addOnFailureListener { e ->
                completionSource.setResult(false)
            }

        return completionSource.task
    }


}

