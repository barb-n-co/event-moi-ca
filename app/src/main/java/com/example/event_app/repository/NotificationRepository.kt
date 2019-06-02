package com.example.event_app.repository

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.event_app.R
import com.example.event_app.model.Message
import com.example.event_app.ui.activity.SplashScreenActivity
import com.google.firebase.database.FirebaseDatabase
import timber.log.Timber

class NotificationRepository(private val context: Context) {

    private val messageRef = FirebaseDatabase.getInstance().getReference("messages")

    fun sendMessageToSpecificChannel(eventOwner: String, eventId: String) {
        messageRef.push().setValue(Message(context.getString(R.string.notification_message_title), eventOwner, eventId))
        Timber.d("message push to db")
    }

    fun createNotificationChannel(channelId: String) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val descriptionText = context.getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelId, importance).apply {
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
        dataEventOwner: String,
        dataEventId: String,
        context: Context
    ) {

        val intent = Intent(context, SplashScreenActivity::class.java)
        intent.putExtra("title", dataTitle)
        intent.putExtra("eventOwner", dataEventOwner)
        intent.putExtra("eventId", dataEventId)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        Timber.tag("subscribed").d(dataEventId)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_ONE_SHOT
        )

        val notificationBuilder = NotificationCompat.Builder(context, dataEventOwner)
            .setSmallIcon(R.drawable.logo_event_moi_ca_notif)
            .setColor(Color.GREEN)
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
