package com.example.event_app.viewmodel

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.event_app.model.User
import com.example.event_app.repository.UserRepository
import com.example.event_app.ui.activity.SplashScreenActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class SplashScreenViewModel(private val userRepository: UserRepository) : BaseViewModel() {

    fun getCurrentUser(): FirebaseUser? {
        val user = userRepository.fireBaseAuth.currentUser
        user?.let {
            userRepository.currentUser.onNext(User(it.uid, it.displayName, it.email))
        }
        return user
    }

    fun initMessageReceiving() {

        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Timber.w(task.exception, "getInstanceId failed")
                    return@OnCompleteListener
                }

                // Get new Instance ID token
                val token = task.result?.token

                // Log and toast
                val msg = "message with token = $token"
                Timber.d(msg)
            })

        FirebaseMessaging.getInstance().subscribeToTopic("notif_event_moi_ca")
            .addOnCompleteListener { task ->
                var msg = "subscribed !!!"
                if (!task.isSuccessful) {
                    msg = "failed to subscribed"
                }
                Timber.d("message for subscribing: $msg")
            }
    }

    @Throws(IOException::class)
    fun createImageFile(context: Context, uri: Uri): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            val enter = context.contentResolver.openInputStream(uri)
            val out = FileOutputStream(this)
            val buf = ByteArray(1024)
            var len = enter.read(buf)
            while (len > 0) {
                out.write(buf, 0, len)
                len = enter.read(buf)
            }
            out.close()
            enter!!.close()
            // Save a file: path for use with ACTION_VIEW intents
            SplashScreenActivity.sharedPhotoPath = absolutePath
        }
    }


    class Factory(private val userRepository: UserRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return SplashScreenViewModel(userRepository) as T
        }
    }
}
