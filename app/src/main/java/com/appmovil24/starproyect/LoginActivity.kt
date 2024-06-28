package com.appmovil24.starproyect

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.appmovil24.starproyect.helper.Helper
import com.appmovil24.starproyect.model.UserAccountDTO
import com.appmovil24.starproyect.repository.UserAccountRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (FirebaseAuth.getInstance().currentUser != null)
            startApplication()
        else
            renderLogin()
    }

    private fun renderLogin() {
        setContentView(R.layout.activity_login);
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        findViewById<SignInButton>(R.id.sign_in_button).setOnClickListener {
            var googleSignInClient = GoogleSignIn.getClient(this, signInOptions)
            signInResultLauncher.launch(googleSignInClient.signInIntent)
        }
    }

    val signInResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    val account = task.getResult(ApiException::class.java)!!
                    onLoginSuccess(account.idToken!!)
                } catch (e: ApiException) {
                    Helper.showToast(this, "No fue posible iniciar sesion: ${e.message}");
                }
            }
        }

    private fun onLoginSuccess(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        Firebase.auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful && Firebase.auth.currentUser != null)
                    startApplication()
                else
                    Helper.showToast(this, "Fallo de autenticacion: ${task.exception?.message}")
            }
    }

    private fun startApplication() {
        val userAccountRepository = UserAccountRepository(Firebase.auth.currentUser !!)

        userAccountRepository.get { userAccountDTO: UserAccountDTO? ->
            val intent: Intent
            if (userAccountDTO != null)
                intent = Intent(this@LoginActivity, MainActivity::class.java)
            else
                intent = Intent(this@LoginActivity, RegisterUserFormActivity::class.java)

            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }

}