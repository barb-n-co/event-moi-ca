package com.example.event_app.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.event_app.R
import com.example.event_app.adapter.ListEventAdapter
import com.example.event_app.model.Event
import com.example.event_app.utils.GlideApp
import com.example.event_app.viewmodel.ShareGalleryViewModel
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_share_gallery.*
import org.kodein.di.generic.instance
import timber.log.Timber


class ShareGalleryActivity : BaseActivity() {

    private val viewModel: ShareGalleryViewModel by instance(arg = this)
    lateinit var imageUri: Uri
    private var eventId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share_gallery)


        val intent = intent
        val type = intent.type
        val action = intent.action

        val user = viewModel.getCurrentUser()
        Handler().postDelayed({
            if (user != null) {
                Log.d("ShareGallery", "user : " + user.uid)
                if (Intent.ACTION_SEND == action && type != null) {
                    if (type.startsWith("image/")) {
                        imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM) as Uri
                        if (imageUri != null) {
                            GlideApp.with(this).load(imageUri).into(iv_imageToShare)
                            viewModel.eventList.subscribe(
                                {
                                    initAdapter(it)
                                },
                                {
                                    Timber.e(it)
                                })
                                .addTo(viewDisposable)
                            viewModel.getEvents()
                        } else {
                            Toast.makeText(
                                this,
                                "Une erreur est survenue, merci de ressayer plus tard",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Une erreur est survenue, merci de ressayer plus tard", Toast.LENGTH_LONG)
                        .show()
                }

            } else {
                LoginActivity.start(this)
            }
        }, 2000L)

    }

    private fun initAdapter(eventList: List<Event>) {
        Log.d("ShareGallery", "event :" + eventList.size)
        val adapter = ListEventAdapter()
        val mLayoutManager = LinearLayoutManager(this)

        val galeryBitmap = viewModel.getBitmapWithResolver(this.contentResolver, imageUri)

        rv_shareEvent.layoutManager = mLayoutManager
        rv_shareEvent.itemAnimator = DefaultItemAnimator()
        rv_shareEvent.adapter = adapter
        adapter.submitList(eventList)

        adapter.eventsClickPublisher.subscribe(
            {
                Log.d("ShareGal", "it : "+it)
                    viewModel.putImageWithBitmap(galeryBitmap, it)

            },
            {
                Timber.e(it)
            }
        ).addTo(viewDisposable)
    }
}
