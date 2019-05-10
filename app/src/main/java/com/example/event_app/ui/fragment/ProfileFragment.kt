package com.example.event_app.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.event_app.R
import com.example.event_app.model.NumberEvent
import com.example.event_app.model.User
import com.example.event_app.ui.activity.LoginActivity
import com.example.event_app.utils.or
import com.example.event_app.viewmodel.ProfileViewModel
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_profile.*
import org.kodein.di.generic.instance
import timber.log.Timber

class ProfileFragment: BaseFragment() {

    private val viewModel: ProfileViewModel by instance(arg = this)

    companion object {
        const val TAG = "PROFILERAGMENT"
        fun newInstance(): ProfileFragment = ProfileFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        b_deconnexion_profile_fragment.setOnClickListener {
            actionDeconnexion()
        }

        b_delete_account_profile_fragment.setOnClickListener {
            actionDeleteAccount()
        }

        viewModel.user.subscribe(
            {
                initUser(it)
            },
            {
                Timber.e(it)
            }
        ).addTo(viewDisposable)

        viewModel.eventCount.subscribe(
            {
                initNumberEvent(it)
            },
            {
                Timber.e(it)
            }
        ).addTo(viewDisposable)
    }



    private fun actionDeconnexion() {
        val dialog = AlertDialog.Builder(activity!!)
        dialog.setTitle(R.string.tv_title_dialog_logout)
            .setMessage(R.string.tv_message_dialog_logout)
            .setNegativeButton(R.string.b_cancel_dialog) { dialoginterface, i -> }
            .setPositiveButton(R.string.b_validate_dialog) { dialoginterface, i ->
                viewModel.logout()
                LoginActivity.start(activity!!)
                activity!!.finish()
            }.show()
    }

    private fun actionDeleteAccount() {
        val dialog = AlertDialog.Builder(activity!!)
        dialog.setTitle(R.string.tv_title_dialog_delete_account)
            .setMessage(R.string.tv_message_dialog_delete_account)
            .setNegativeButton(R.string.b_cancel_dialog) { dialoginterface, i -> }
            .setPositiveButton(R.string.b_validate_dialog) { dialoginterface, i ->
                viewModel.deleteAccount()
                LoginActivity.start(activity!!)
                activity!!.finish()
            }.show()
    }

    private fun initUser(user: User) {
        tv_name_fragment_profile.text = user.name
        tv_email_fragment_profile.text = user.email
    }

    private fun initNumberEvent(numberEvent: NumberEvent){
        tv_event_invitation_fragment_profile.text = resources.getString(R.string.tv_number_invitation_profile_fragment, numberEvent.invitation)
        tv_event_participate_fragment_profile.text = resources.getString(R.string.tv_number_participate_profile_fragment, numberEvent.participate)
        tv_event_organizer_fragment_profile.text = resources.getString(R.string.tv_number_organizer_profile_fragment, numberEvent.organizer)
    }

    override fun onStart() {
        super.onStart()
        viewModel.getCurrentUser()
        viewModel.getNumberEventUser()
    }

}