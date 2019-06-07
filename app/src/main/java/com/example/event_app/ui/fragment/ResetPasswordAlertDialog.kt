package com.example.event_app.ui.fragment

import android.content.Context
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.event_app.R
import com.example.event_app.viewmodel.LoginViewModel
import kotlinx.android.synthetic.main.reset_password_alert_dialog.view.*


class ResetPasswordAlertDialog(
    private var title: String,
    private var message: String,
    private var positiveButtonTitle: String,
    private var negativeButtonTitle: String,
    private var emptyAlert: String,
    private var loginViewModel: LoginViewModel
) {

    fun showOpenDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        val view = LayoutInflater.from(context).inflate(R.layout.reset_password_alert_dialog, null)
        builder.setView(view)

        builder.setTitle(title)
        builder.setMessage(message)
        view.ed_reset_password.hint = "E-Mail"

        builder.setPositiveButton(positiveButtonTitle) { _, _ ->
            val email = view.ed_reset_password.text.toString()
            if (email.isEmpty()) {
                Toast.makeText(
                    view.context,
                    emptyAlert,
                    Toast.LENGTH_LONG
                ).show()
            } else {
                loginViewModel.resetPassword(email)
            }
        }


        builder.setNegativeButton(negativeButtonTitle
        ) { dialog, _ -> dialog.dismiss() }


        val alertDialog = builder.create()
        alertDialog.show()
    }
}