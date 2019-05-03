package com.example.event_app.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.event_app.R
import com.example.event_app.ui.activity.MainActivity
import com.example.event_app.viewmodel.LoginViewModel
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_signup.*
import org.kodein.di.generic.instance
import timber.log.Timber

class LoginFragment: BaseFragment() {

    private val viewModel: LoginViewModel by instance(arg = this)

    companion object {
        const val TAG = "LOGINFRAGMENT"
        fun newInstance(): LoginFragment = LoginFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        b_signin_fragment.setOnClickListener {
            val email = et_email_signin_fragment.text.toString()
            val password = et_password_signin_fragment.text.toString()
            userLogin(email, password)
        }
    }

    private fun userLogin(email: String, password: String) {
        viewModel.logIn(email, password).subscribe(
            {
                if(it) MainActivity.start(activity!!)
            },
            {
                Timber.e(it)
                Toast.makeText(context,"${it.message}", Toast.LENGTH_SHORT).show()
            }
        ).addTo(viewDisposable)
    }
}