package com.example.event_app.ui.activity

import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.example.event_app.R
import com.example.event_app.model.User
import com.example.event_app.repository.UserRepository

class SplashScreenActivity : AppCompatActivity() {

    private var userRepository = UserRepository.getInstance(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        val user = userRepository.fireBaseAuth.currentUser

        Handler().postDelayed({
            if (user != null) {
                userRepository.currentUser = User(user.uid, user.displayName, user.email, user.photoUrl)
                MainActivity.start(this)
            } else {
                LoginActivity.start(this)
            }
        }, 2000L)



    }
}
