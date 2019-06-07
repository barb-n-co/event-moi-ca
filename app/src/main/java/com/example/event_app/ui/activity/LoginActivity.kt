package com.example.event_app.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.example.event_app.R
import com.example.event_app.adapter.AuthentificationViewPagerAdapter
import com.example.event_app.ui.fragment.LoginFragment
import com.example.event_app.ui.fragment.SignUpFragment
import kotlinx.android.synthetic.main.activity_authentification.*
import java.io.File

class LoginActivity : BaseActivity() {

    companion object {
        var file: File? = null
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
        adapter.addFragment(SignUpFragment.newInstance(), getString(R.string.ti_signup_authentification_activity))
        vp_sign_authentification_fragment.adapter = adapter
        tl_sign_authentification_fragment.setupWithViewPager(vp_sign_authentification_fragment)
    }

    override fun onStop() {
        super.onStop()
        finish()
    }
}