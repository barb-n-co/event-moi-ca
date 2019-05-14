package com.example.event_app.ui.activity

import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein

abstract class BaseActivity : AppCompatActivity(), KodeinAware {


    protected val viewDisposable: CompositeDisposable = CompositeDisposable()
    override val kodein by closestKodein()

    override fun onDestroy() {
        super.onDestroy()
        viewDisposable.dispose()
    }

}