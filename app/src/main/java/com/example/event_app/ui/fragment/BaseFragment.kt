package com.example.event_app.ui.fragment

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.event_app.ui.activity.MainActivity
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

abstract class BaseFragment: Fragment(), KodeinAware{

    override val kodein by closestKodein()
    private val viewDisposable: CompositeDisposable = CompositeDisposable()

    override fun onDestroyView() {
        viewDisposable.clear()
        super.onDestroyView()
    }

    protected fun setTitleToolbar(title : String) {
        (activity as MainActivity).supportActionBar?.title = title
    }

    protected fun setDisplayHomeAsUpEnabled(value : Boolean) {
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(value)
    }

    protected fun closeMainActivity(){
        (activity as MainActivity).finish()
    }

}