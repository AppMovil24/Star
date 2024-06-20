package com.appmovil24.starproyect.repository
import com.appmovil24.starproyect.model.UserAccountDTO
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class UserAccountRepository(private val currentFirebaseUser: FirebaseUser?) {

    private val usersCollection = Firebase.firestore.collection("usuarios")

    // Método para obtener un usuario
    fun get(onComplete: (UserAccountDTO?) -> Unit) {
        val userEmail = currentFirebaseUser?.email
        if (!userEmail.isNullOrEmpty()) {
            val userRef = usersCollection.whereEqualTo("email", userEmail).limit(1)

            userRef.get()
                .addOnSuccessListener { documents ->
                    if (documents != null && !documents.isEmpty) {
                        val userAccountDTO = documents.documents[0].toObject<UserAccountDTO>()
                        onComplete(userAccountDTO)
                    } else {
                        onComplete(null)
                    }
                }
                .addOnFailureListener { exception ->
                    onComplete(null)
                }
        } else
            onComplete(null)
    }

    // Método para crear un usuario
    fun add(userAccountDTO: UserAccountDTO, onComplete: (Boolean) -> Unit) {
        val userEmail = currentFirebaseUser?.email
        if (!userEmail.isNullOrEmpty()) {
            usersCollection.document(userAccountDTO.id)
                .set(userAccountDTO)
                .addOnSuccessListener {
                    onComplete(true)
                }
                .addOnFailureListener { exception ->
                    onComplete(false)
                }
        } else {
            onComplete(false)
        }
    }

    // Método para verificar si un usuarioId está en uso
    fun exists(usuarioId: String, onComplete: (Boolean) -> Unit) {
        val userRef = usersCollection.document(usuarioId)
        userRef.get()
            .addOnSuccessListener { document ->
                onComplete(document.exists())
            }
            .addOnFailureListener { exception ->
                onComplete(false)
            }
    }

}



