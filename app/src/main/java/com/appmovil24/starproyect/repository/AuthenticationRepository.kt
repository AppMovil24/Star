package com.appmovil24.starproyect.repository

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.appmovil24.starproyect.activity.Login
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AuthenticationRepository {

    companion object {
        @JvmStatic
        public fun signOut(activity: AppCompatActivity) {
            Firebase.auth.signOut()
            logIn(activity)
        }

        @JvmStatic
        public fun logIn(activity: AppCompatActivity) {
            val intent = Intent(activity, Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            activity.startActivity(intent)
            activity.finish()
        }
    }

}