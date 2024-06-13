package com.appmovil24.starproyect

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.appmovil24.starproyect.model.UserAccount
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.auth.User
import com.google.firebase.ktx.Firebase

class Login : AppCompatActivity(), View.OnClickListener {

    private lateinit var signInButton: SignInButton

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    private val signInResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e)
                showToast("Google sign-in failed: ${e.message}");
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, signInOptions)
        auth = Firebase.auth

        setContentView(R.layout.login);
        signInButton = findViewById<SignInButton>(R.id.sign_in_button)
        signInButton.setOnClickListener(this);
    }

    override fun onClick(view: View?) {
        if(view != null)
            when(view.id) {
                R.id.sign_in_button -> signIn()
            }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        signInResultLauncher.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if(user !=null)
                        initUserInformation(user)
                    else {
                        Log.w(TAG, "Error al iniciar sesion : auth.currentUser = null")
                        showToast("Error al iniciar sesion.")
                    }
                } else {
                    Log.w(TAG, "signInWithCredential: error", task.exception)
                    showToast("Fallo de autenticacion: ${task.exception?.message}")
                }
            }
    }

    private fun initUserInformation(user: FirebaseUser) {
        val user = Firebase.auth.currentUser
        user?.let {
            val userId = user.uid
            val database = FirebaseDatabase.getInstance()
            val myRef = database.getReference("users/$userId")

            myRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val value = dataSnapshot.getValue(UserAccount::class.java)
                    if (value == null) {
                        val intent = Intent(this@Login, Register::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    }
                    finish()
                }

                override fun onCancelled(error: DatabaseError) {
                    // Error al leer el valor
                    Log.w(TAG, "Failed to read value.", error.toException())
                }
            })
        }
    }


    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val TAG = "GoogleActivity"
        private const val RC_SIGN_IN = 9001
    }
}