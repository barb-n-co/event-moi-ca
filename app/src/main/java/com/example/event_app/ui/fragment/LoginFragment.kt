package com.example.event_app.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.event_app.R
import com.example.event_app.ui.activity.MainActivity
import com.example.event_app.ui.activity.ShareGalleryActivity
import com.example.event_app.ui.activity.SplashScreenActivity
import com.example.event_app.utils.toast
import com.example.event_app.viewmodel.LoginViewModel
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_login.*
import org.kodein.di.generic.instance
import timber.log.Timber


class LoginFragment : BaseFragment() {

    private val viewModel: LoginViewModel by instance(arg = this)

    companion object {
        fun newInstance(): LoginFragment = LoginFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        b_signin_fragment.setOnClickListener {
            val email = et_email_signin_fragment.text.toString()
            val password = et_password_signin_fragment.text.toString()
            if (!viewModel.checkIfFieldsAreEmpty(email, password)) {
                userLogin(email, password)
            } else {
                context?.toast(R.string.login_fragment_error_emplty, Toast.LENGTH_SHORT)
            }

        }

        b_reset_password_signin_fragment.setOnClickListener {
            val resetPassDialog = ResetPasswordAlertDialog(
                getString(R.string.tv_title_reset_password_fragment),
                getString(R.string.tv_message_reset_password_fragment),
                getString(R.string.b_validate_dialog),
                getString(R.string.b_cancel_dialog),
                getString(R.string.t_empty_email_reset_password_dialog_fragment),
                viewModel
                )
            resetPassDialog.showOpenDialog(context!!)
        }
    }

    private fun userLogin(email: String, password: String) {
        viewModel.logIn(email, password).subscribe(
            {
                if (it && SplashScreenActivity.sharedPhotoPath != null) {
                    val shareGalleryIntent = Intent(context, ShareGalleryActivity::class.java)
                    startActivity(shareGalleryIntent)
                } else if (it) {
                    viewModel.initNotificationChannel()
                    MainActivity.start(activity!!)
                }
            },
            {
                Timber.e(it)
                context?.toast(it.localizedMessage, Toast.LENGTH_SHORT)
            }
        ).addTo(viewDisposable)
    }
}
