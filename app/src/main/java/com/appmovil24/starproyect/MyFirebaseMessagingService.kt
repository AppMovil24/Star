package com.appmovil24.starproyect

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.FirebaseMessaging
import java.util.UUID

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        var deviceToken: String = ""
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        deviceToken = token
    }

    fun sendNotificationToUser(deviceToken: String, titulo: String, cuerpo: String) {
        val senderId = "316019475338"
        val messageId = UUID.randomUUID().toString()

        FirebaseMessaging.getInstance().send(
            RemoteMessage.Builder("${Companion.deviceToken}@fcm.googleapis.com")
                .setMessageId(messageId)
                .addData("title", titulo)
                .addData("body", cuerpo)
                .build()
        )
    }
}
