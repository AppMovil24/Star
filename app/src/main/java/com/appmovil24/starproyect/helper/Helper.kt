package com.appmovil24.starproyect.helper

import android.app.Activity
import android.widget.Toast

class Helper {

    companion object {
        fun showToast(context: Activity, message: String) {
            context.runOnUiThread {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

}