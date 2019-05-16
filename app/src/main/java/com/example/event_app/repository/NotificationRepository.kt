package com.example.event_app.repository

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.event_app.R
import com.example.event_app.model.Message
import com.example.event_app.ui.activity.SplashScreenActivity
import com.google.firebase.database.FirebaseDatabase
import timber.log.Timber

class NotificationRepository(private val context: Context) {

    private val CHANNEL_ID = "notif_event_moi_ca"
    private val messageRef = FirebaseDatabase.getInstance().getReference("messages")
    private var currentUserId: String = UserRepository.currentUser.value?.id ?: ""

    fun sendMessageToSpecificChannel(eventOwner: String) {
        createNotificationChannel()
        messageRef.push().setValue(Message(context.getString(R.string.notification_message_title), eventOwner))
        Timber.d("message sent")
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = CHANNEL_ID
            val descriptionText = context.getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendNotification(
        notificationTitle: String,
        notificationBody: String,
        dataTitle: String,
        dataMessage: String,
        context: Context
    ) {

        if (currentUserId == dataMessage) {
            val intent = Intent(context, SplashScreenActivity::class.java)
            intent.putExtra("title", dataTitle)
            intent.putExtra("message", dataMessage)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_ONE_SHOT
            )

            val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.smartphone)
                .setContentTitle(notificationTitle)
                .setContentText(notificationBody)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)

            with(NotificationManagerCompat.from(context)) {
                notify(1, notificationBuilder.build())
            }
        }
    }

}
