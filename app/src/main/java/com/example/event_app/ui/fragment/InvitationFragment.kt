package com.example.event_app.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.event_app.R
import com.example.event_app.viewmodel.HomeFragmentViewModel
import org.kodein.di.direct
import org.kodein.di.generic.instance

private lateinit var viewModel: HomeFragmentViewModel

class InvitationFragment : BaseFragment() {
    companion object {
        const val TAG = "INVITATIONFRAGMENT"
        fun newInstance(): InvitationFragment = InvitationFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_invitation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = kodein.direct.instance(arg = this)
    }
}