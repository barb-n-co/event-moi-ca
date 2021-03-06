package com.example.event_app.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.event_app.R
import com.example.event_app.ui.activity.MainActivity
import com.example.event_app.utils.toast
import com.example.event_app.viewmodel.LoginViewModel
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_signup.*
import org.kodein.di.generic.instance
import timber.log.Timber

class SignUpFragment : BaseFragment() {


    private val viewModel: LoginViewModel by instance(arg = this)

    companion object {
        fun newInstance(): SignUpFragment = SignUpFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_signup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        b_signup_fragment.setOnClickListener {
            val firstName = et_firstname_signup_fragment.text.toString()
            val lastName = et_lastname_signup_fragment.text.toString()
            val fullName = "$firstName $lastName"
            val email = et_email_signup_fragment.text.toString()
            val password = et_password_signup_fragment.text.toString()
            val confirmPassword = et_confirm_password_signup_fragment.text.toString()
            if (password == confirmPassword) {
                userRegister(email, password, fullName)
            } else {
                context?.toast(R.string.passwords_does_not_match, Toast.LENGTH_SHORT)
            }

        }
        tv_cgu.setOnClickListener { openCGU() }
    }

    private fun openCGU() {
        val popup = CguDialogFragment()
        popup.show(requireFragmentManager(), "CGU")
    }

    private fun userRegister(email: String, password: String, name: String) {
        if (cb_cgu.isChecked) {
            tv_cgu.setTextColor(ContextCompat.getColor(this.context!!, R.color.white))
            viewModel.register(email, password, name, context!!).subscribe(
                {
                    if (it) MainActivity.start(activity!!)
                    viewModel.setEmptyEvent()
                },
                {
                    Timber.e(it)
                    context?.toast(R.string.login_fragment_error_emplty, Toast.LENGTH_SHORT)
                }
            ).addTo(viewDisposable)
        } else {
            tv_cgu.setTextColor(ContextCompat.getColor(this.context!!, R.color.error))
        }
    }

}
