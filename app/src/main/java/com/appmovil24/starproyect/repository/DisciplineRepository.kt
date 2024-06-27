package com.appmovil24.starproyect.repository
import com.appmovil24.starproyect.model.DisciplineDTO
import com.appmovil24.starproyect.model.UserAccountDTO
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class DisciplineRepository() {

    private val usersCollection = Firebase.firestore.collection("disciplinas")

    fun get(disciplineName: String, onComplete: (DisciplineDTO?) -> Unit) {
        val userRef = usersCollection.whereEqualTo("name", disciplineName).limit(1)

        userRef.get()
            .addOnSuccessListener { documents ->
                if (documents != null && !documents.isEmpty)
                    onComplete( documents.documents[0].toObject<DisciplineDTO>())
                else
                    onComplete(null)
            }
            .addOnFailureListener { exception ->
                onComplete(null)
            }
    }

}



