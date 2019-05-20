package com.example.event_app.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.FragmentActivity
import com.example.event_app.R
import com.example.event_app.viewmodel.SplashScreenViewModel
import org.kodein.di.generic.instance
import timber.log.Timber


class SplashScreenActivity : BaseActivity() {

    private val viewModel: SplashScreenViewModel by instance(arg = this)
    private lateinit var intentReceived: Intent

    companion object {
        fun start(fromActivity: FragmentActivity) {
            fromActivity.startActivity(
                Intent(fromActivity, SplashScreenActivity::class.java)
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        intentReceived = intent
        val type = intentReceived.type
        val action = intentReceived.action

        val user = viewModel.getCurrentUser()
        Handler().postDelayed({
            if (user != null && Intent.ACTION_SEND == action && type.startsWith("image/")) {
                Timber.tag("TEST_").d("1")
                val imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM) as Uri
                val shareGalleryIntent = Intent(this, ShareGalleryActivity::class.java)
                shareGalleryIntent.putExtra("uri",imageUri)
                startActivity(shareGalleryIntent)
            }else if (user != null) {
                Timber.tag("TEST_").d("2")
                MainActivity.start(this)
            } else {
                Timber.tag("TEST_").d("3")
                LoginActivity.start(this)
            }
        }, 2000L)

        viewModel.initMessageReceiving()
    }

    override fun onStop() {
        super.onStop()
        finish()
    }
}
