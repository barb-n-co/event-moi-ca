package com.example.event_app.ui.activity

import android.os.Bundle
import android.os.Handler
import com.example.event_app.R
import com.example.event_app.viewmodel.SplashScreenViewModel
import org.kodein.di.generic.instance


class SplashScreenActivity : BaseActivity() {

    private val viewModel : SplashScreenViewModel by instance(arg = this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        val user = viewModel.getCurrentUser()
        Handler().postDelayed({
            if (user != null) {
                MainActivity.start(this)
            } else {
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
