package com.example.event_app.ui.fragment

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.event_app.manager.PermissionManager
import com.example.event_app.ui.activity.MainActivity
import io.reactivex.disposables.CompositeDisposable
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

abstract class BaseFragment: Fragment(), KodeinAware{

    override val kodein by closestKodein()
    val viewDisposable: CompositeDisposable = CompositeDisposable()
    protected val permissionManager: PermissionManager by instance()

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