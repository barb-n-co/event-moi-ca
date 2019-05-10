package com.example.event_app.ui.fragment

import com.example.event_app.viewmodel.LoginViewModel
import com.example.event_app.viewmodel.ModifyEventViewModel
import org.kodein.di.generic.instance

class ModifyEventFragment : BaseFragment(){
    private val viewModel: ModifyEventViewModel by instance(arg = this)

    companion object {
        const val TAG = "MODIFYFRAGMENT"
        fun newInstance(): ModifyEventFragment = ModifyEventFragment()
    }
}