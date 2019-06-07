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
                Toast.makeText(context, getString(R.string.login_fragment_error_emplty), Toast.LENGTH_SHORT).show()
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
//            val alertDialog = AlertDialog.Builder(activity!!)
//            alertDialog.setTitle(getString(R.string.tv_title_reset_password_fragment))
//            alertDialog.setMessage(getString(R.string.tv_message_reset_password_fragment))
//
//            val input = EditText(activity!!)
//            val lp = LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.WRAP_CONTENT
//            )
//            input.hint = "E-Mail"
//            input.layoutParams = lp
//            alertDialog.setView(input)
//
//            alertDialog.setPositiveButton(
//                getString(R.string.b_validate_dialog)
//            ) { _ , _ ->
//                val email = input.text.toString()
//                if (email.isEmpty()) {
//                    Toast.makeText(
//                        activity!!,
//                        getString(R.string.t_empty_email_reset_password_dialog_fragment),
//                        Toast.LENGTH_LONG
//                    ).show()
//                } else {
//                    viewModel.resetPassword(email)
//                }
//            }
//
//            alertDialog.setNegativeButton(
//                getString(R.string.b_cancel_dialog)
//            ) { dialog, _ -> dialog.cancel() }
//
//            alertDialog.show()
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
                Toast.makeText(context, it.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        ).addTo(viewDisposable)
    }
}
