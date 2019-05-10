package com.example.event_app.ui.activity

import android.view.Menu
import android.view.MenuInflater
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance

abstract class BaseActivity : AppCompatActivity(), KodeinAware {


    protected val viewDisposable: CompositeDisposable = CompositeDisposable()
    override val kodein by closestKodein()


}