package com.example.event_app.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.event_app.R
import com.example.event_app.repository.UserRepository
import com.example.event_app.viewmodel.LoginViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance
import timber.log.Timber



class LoginActivity : BaseActivity() {

    private lateinit var mAuth: FirebaseAuth
    private var userRepository = UserRepository.getInstance(this)
    private val viewModel: LoginViewModel by instance(arg = this)

    companion object {
        fun start(fromActivity: AppCompatActivity) {
            fromActivity.startActivity(
                Intent(fromActivity, LoginActivity::class.java)
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        switch_login_register.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                buttonView.text = "Login"
                loginButton.text = "Login"
            } else {
                buttonView.text = "Register"
                loginButton.text = "Register"
            }
        }

        // Initialize Firebase Auth

        mAuth = userRepository.fireBaseAuth
        val myRef = userRepository.usersRef


        // Read from the database
        userRepository.addListenerOnRef(myRef)


        loginButton.setOnClickListener {
            val email = (email_et.text ?: "").toString()
            val password = (password_et.text ?: "").toString()

            if (loginButton.text.toString() == "Login") {
                userLogin(email,password)
            } else {
                userRegister(email, password)
            }
        }
    }

    private fun userLogin(email: String, password: String) {
        viewModel.logIn(email, password).subscribe(
            {
                MainActivity.start(this)
            },
            {
                Timber.e(it)
            }
        )
    }

    override fun onStop() {
        super.onStop()
        finish()
    }


    private fun userRegister(email: String, password: String) {
        userRepository.registerUser(email, password)
    }


    override fun onStart() {
        super.onStart()
        val currentUser = mAuth.currentUser
        Timber.d( "current User : $currentUser")
        if (currentUser != null) {
            // Name, email address, and profile photo Url
            val name = currentUser.displayName
            val email = currentUser.email
            val photoUrl = currentUser.photoUrl
            // Check if user's email is verified
            val emailVerified = currentUser.isEmailVerified

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getIdToken() instead.
            val uid = currentUser.uid
            Timber.d( "User info : uid = $uid , name = $name , email = $email , photo URL = $photoUrl , is email verified ? $emailVerified")
        }
    }
}