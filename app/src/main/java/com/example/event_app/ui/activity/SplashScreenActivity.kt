package com.example.event_app.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.FragmentActivity
import com.example.event_app.R
import com.example.event_app.viewmodel.SplashScreenViewModel
import io.reactivex.rxkotlin.addTo
import org.kodein.di.generic.instance
import timber.log.Timber


class SplashScreenActivity : BaseActivity() {

    private val viewModel: SplashScreenViewModel by instance(arg = this)
    private lateinit var intentReceived: Intent

    companion object {
        var sharedPhotoPath: String? = null
        fun start(fromActivity: FragmentActivity) {
            fromActivity.startActivity(
                Intent(fromActivity, SplashScreenActivity::class.java)
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        supportActionBar?.hide()

        intentReceived = intent
        val type = intentReceived.type
        val action = intentReceived.action

        viewModel.endSplashscreen.subscribe(
            {
                    when {
                        it && Intent.ACTION_SEND == action && type?.startsWith("image/") ?: false -> {
                            val imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM) as Uri
                            val shareGalleryIntent = Intent(this, ShareGalleryActivity::class.java)
                            shareGalleryIntent.putExtra("uri", imageUri)
                            startActivity(shareGalleryIntent)
                        }
                        it -> {
                            MainActivity.start(this)
                        }
                        Intent.ACTION_SEND == action && type?.startsWith("image/") ?: false -> {
                            val imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM) as Uri
                            viewModel.createImageFile(this, imageUri)
                            LoginActivity.start(this)
                        }
                        else -> {
                            LoginActivity.start(this)
                        }
                    }
                finish()
            },
            {
                Timber.e(it)
            }
        ).addTo(viewDisposable)

    }

    override fun onResume() {
        super.onResume()
        viewModel.getCurrentUser()
    }
}
