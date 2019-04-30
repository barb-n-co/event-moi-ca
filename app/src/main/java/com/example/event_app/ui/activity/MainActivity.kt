package com.example.event_app.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.event_app.R
import com.example.event_app.repository.UserRepository

class MainActivity : BaseActivity() {

    private var userRepository = UserRepository.getInstance(this)
    companion object {

        fun start(fromActivity: AppCompatActivity) {
            fromActivity.startActivity(
                Intent(fromActivity, MainActivity::class.java)
            )
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Toast.makeText(this, "Current User : ${userRepository.currentUser}", Toast.LENGTH_LONG).show()
    }
}




