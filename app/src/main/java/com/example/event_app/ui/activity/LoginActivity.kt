package com.example.event_app.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.example.event_app.R
import com.example.event_app.adapter.AuthentificationViewPagerAdapter
import com.example.event_app.ui.fragment.LoginFragment
import com.example.event_app.ui.fragment.SignupFragment
import com.example.event_app.viewmodel.LoginViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_authentification.*
import org.kodein.di.generic.instance
import timber.log.Timber

class LoginActivity : BaseActivity() {

    private lateinit var mAuth: FirebaseAuth
    private val viewModel: LoginViewModel by instance(arg = this)

    companion object {
        fun start(fromActivity: FragmentActivity) {
            fromActivity.startActivity(
                Intent(fromActivity, LoginActivity::class.java)
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentification)
        setupViewPager()
    }

    private fun setupViewPager() {
        val adapter = AuthentificationViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(LoginFragment.newInstance(), getString(R.string.ti_login_authentification_activity))
        adapter.addFragment(SignupFragment.newInstance(), getString(R.string.ti_signup_authentification_activity))
        vp_sign_authentification_fragment.adapter = adapter
        tl_sign_authentification_fragment.setupWithViewPager(vp_sign_authentification_fragment)
    }

    override fun onStop() {
        super.onStop()
        finish()
    }
}