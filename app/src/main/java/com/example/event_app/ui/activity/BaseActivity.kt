package com.example.event_app.ui.activity

import androidx.appcompat.app.AppCompatActivity
import com.example.event_app.manager.PermissionManager
import io.reactivex.disposables.CompositeDisposable
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance

abstract class BaseActivity : AppCompatActivity(), KodeinAware {


    protected val viewDisposable: CompositeDisposable = CompositeDisposable()
    override val kodein by closestKodein()
    protected val permissionManager: PermissionManager by instance()

    override fun onDestroy() {
        super.onDestroy()
        viewDisposable.dispose()
    }

}