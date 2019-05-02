package com.example.event_app.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.event_app.R
import com.example.event_app.viewmodel.LoginViewModel
import com.google.firebase.auth.FirebaseAuth
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_login.*
import org.kodein.di.generic.instance
import timber.log.Timber

class LoginActivity : BaseActivity() {

    private lateinit var mAuth: FirebaseAuth
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

        mAuth = viewModel.getFirebaseAuth()


        // Read from the database
        //userRepository.addListenerOnRef(myRef)


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
                if(it) MainActivity.start(this)
            },
            {
                Timber.e(it)
                Toast.makeText(this,"${it.message}",Toast.LENGTH_SHORT).show()
            }
        ).addTo(viewDisposable)
    }

    override fun onStop() {
        super.onStop()
        finish()
    }


    private fun userRegister(email: String, password: String) {
        viewModel.register(email, password).subscribe(
            {
                if(it) MainActivity.start(this)
            },
            {
                Timber.e(it)
                Toast.makeText(this,"${it.message}",Toast.LENGTH_SHORT).show()
            }
        ).addTo(viewDisposable)
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

        val intent = Intent(this, PhotoActivity::class.java)

        startActivity(intent)
    }
}