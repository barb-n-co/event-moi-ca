package com.example.event_app.repository

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import timber.log.Timber


class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val notificationRepository = NotificationRepository(this)
    private val userRepository = UserRepository

    companion object {
        var token: String? = null
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        var notificationTitle: String? = null
        var notificationBody: String? = null
        var dataTitle: String? = null
        var dataEventOwner: String? = null
        var dataEventID: String? = null

        Timber.tag("YOOOLOOO").d("message received ${remoteMessage?.data}")

        // Check if message contains a data payload.
        if (remoteMessage?.data?.size ?: 0 > 0) {
            Timber.d("Message data payload: ${remoteMessage?.data?.get("message")}")
            dataTitle = remoteMessage?.data?.get("title") ?: "empty data title"
            dataEventOwner = remoteMessage?.data?.get("message") ?: "empty message"
            dataEventID = remoteMessage?.data?.get("eventId") ?: "empty Id"
        }

        // Check if message contains a notification payload.
        if (remoteMessage?.notification != null) {
            Timber.d("Message Notification Body: ${remoteMessage.notification?.body}")
            notificationTitle = remoteMessage.notification?.title ?: "empty notification title"
            notificationBody = remoteMessage.notification?.body ?: "empty body"
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
        userRepository.fireBaseAuth.currentUser?.uid?.let {
            if (it == dataEventOwner) {
                notificationRepository.sendNotification(
                    notificationTitle!!,
                    notificationBody!!,
                    dataTitle!!,
                    dataEventOwner,
                    dataEventID!!,
                    this
                )
            }
        }

    }

    override fun onMessageSent(p0: String?) {
        super.onMessageSent(p0)
        Timber.d("message sent : $p0")
    }

    override fun onDeletedMessages() {
        super.onDeletedMessages()
        Timber.d("message deleted")
    }

    override fun onNewToken(newToken: String?) {
        super.onNewToken(newToken)
        token = newToken

        Timber.d("new token: $newToken")
    }


}
