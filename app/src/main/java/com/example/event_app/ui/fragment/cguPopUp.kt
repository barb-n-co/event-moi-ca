package com.example.event_app.ui.fragment

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.event_app.R
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_cgu_pop_up.*

class cguPopUp : DialogFragment() {
    private var viewDisposable : CompositeDisposable = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_cgu_pop_up, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        iv_closeDialog.setOnClickListener { dismiss() }
    }

    override fun onDestroyView() {
        viewDisposable.dispose()
        super.onDestroyView()
    }
}
