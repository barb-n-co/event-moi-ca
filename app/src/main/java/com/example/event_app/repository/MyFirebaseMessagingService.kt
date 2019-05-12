package com.example.event_app.repository

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import timber.log.Timber


class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val notificationRepository = NotificationRepository(this)

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        var notificationTitle: String? = null
        var notificationBody: String? = null
        var dataTitle: String? = null
        var dataMessage: String? = null

        // Check if message contains a data payload.
        if (remoteMessage?.data?.size ?: 0 > 0) {
            Timber.d("Message data payload: ${remoteMessage?.data?.get("message")}")
            dataTitle = remoteMessage?.data?.get("title") ?: "empty data title"
            dataMessage = remoteMessage?.data?.get("message") ?: "empty message"
        }

        // Check if message contains a notification payload.
        if (remoteMessage?.notification != null) {
            Timber.d( "Message Notification Body: ${remoteMessage.notification?.body}")
            notificationTitle = remoteMessage.notification?.title ?: "empty notification title"
            notificationBody = remoteMessage.notification?.body ?: "empty body"
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
        notificationRepository.sendNotification(notificationTitle!!, notificationBody!!, dataTitle!!, dataMessage!!, this)
    }


    override fun onNewToken(p0: String?) {
        super.onNewToken(p0)
        Timber.d("new token: $p0")
    }


}