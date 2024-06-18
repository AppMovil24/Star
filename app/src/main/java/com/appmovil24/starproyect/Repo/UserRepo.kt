package com.appmovil24.starproyect.Repo
import com.appmovil24.starproyect.Model.Usuario
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class UserRepo(private val currentUser: FirebaseUser?) {

    private val db = Firebase.firestore
    private val usersCollection = db.collection("usuarios")

    // Método para obtener un usuario
    fun getUser(onComplete: (Usuario?) -> Unit) {
        val userEmail = currentUser?.email
        if (!userEmail.isNullOrEmpty()) {
            val userRef = usersCollection
                .whereEqualTo("email", userEmail)
                .limit(1)

            userRef.get()
                .addOnSuccessListener { documents ->
                    if (documents != null && !documents.isEmpty) {
                        val user = documents.documents[0].toObject<Usuario>()
                        onComplete(user)
                    } else {
                        onComplete(null)
                    }
                }
                .addOnFailureListener { exception ->
                    onComplete(null)
                }
        } else {
            onComplete(null)
        }
    }

    // Método para crear un usuario
    fun createUser(user: Usuario, onComplete: (Boolean) -> Unit) {
        val userEmail = currentUser?.email
        if (!userEmail.isNullOrEmpty()) {
            usersCollection.document(user.usuarioId)
                .set(user)
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
    fun existe(usuarioId: String, onComplete: (Boolean) -> Unit) {
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



