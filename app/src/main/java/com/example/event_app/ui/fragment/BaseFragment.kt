package com.example.event_app.ui.fragment

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.event_app.manager.PermissionManager
import com.example.event_app.ui.activity.MainActivity
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

abstract class BaseFragment : Fragment(), KodeinAware {

    override val kodein by closestKodein()
    val viewDisposable: CompositeDisposable = CompositeDisposable()
    protected val permissionManager: PermissionManager by instance()

    override fun onDestroyView() {
        viewDisposable.clear()
        super.onDestroyView()
    }

    protected fun setTitleToolbar(title: String) {
        (activity as MainActivity).supportActionBar?.title = title
    }

    protected fun setVisibilityToolbar(value: Boolean) {
        if (value) {
            (activity as MainActivity).supportActionBar?.show()
        } else {
            (activity as MainActivity).supportActionBar?.hide()
        }
    }

    protected fun setDisplayHomeAsUpEnabled(value: Boolean) {
        (activity as MainActivity).supportActionBar?.setDisplayHomeAsUpEnabled(value)
    }

    protected fun closeMainActivity() {
        (activity as MainActivity).finish()
    }

    protected fun setVisibilityNavBar(value: Boolean) {
        if (value) {
            (activity as AppCompatActivity).g_navBar_mainActivity.visibility = View.VISIBLE
        } else (activity as AppCompatActivity).g_navBar_mainActivity.visibility = View.GONE
    }

    protected fun fragmentMapIsOpen(value: Boolean) {
        (activity as MainActivity).isMapOpen(value)
    }
}
