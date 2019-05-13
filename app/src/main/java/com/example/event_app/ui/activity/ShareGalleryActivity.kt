package com.example.event_app.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
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

        viewModel.eventList.subscribe(
            {
                initAdapter(it)
            },
            {
                Timber.e(it)
            })
            .addTo(viewDisposable)
        viewModel.getEvents()

        val user = viewModel.getCurrentUser()
            if (user != null) {
                if (Intent.ACTION_SEND == action && type != null) {
                    if (type.startsWith("image/")) {
                        imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM) as Uri
                        if (imageUri != null) {
                            GlideApp.with(this).load(imageUri).into(iv_imageToShare)
                        } else {
                            Toast.makeText(
                                this,
                                "Une erreur est survenue, merci de ressayer plus tard",
                                Toast.LENGTH_LONG
                            ).show()
                            finish()
                        }
                    }
                } else {
                    Toast.makeText(this, "Une erreur est survenue, merci de ressayer plus tard", Toast.LENGTH_LONG)
                        .show()
                    finish()
                }

            } else {
                LoginActivity.start(this)
            }
    }

    private fun initAdapter(eventList: List<Event>) {
        val adapter = ListEventAdapter()
        val mLayoutManager = LinearLayoutManager(this)

        val galeryBitmap = viewModel.getBitmapWithResolver(this.contentResolver, imageUri)

        rv_shareEvent.layoutManager = mLayoutManager
        rv_shareEvent.itemAnimator = DefaultItemAnimator()
        rv_shareEvent.adapter = adapter
        adapter.submitList(eventList)

        adapter.eventsClickPublisher.subscribe(
            {
                    viewModel.putImageWithBitmap(galeryBitmap, it, true)
                Toast.makeText(this, "Votre image a été envoyé", Toast.LENGTH_LONG)
                    .show()
                finish()
            },
            {
                Timber.e(it)
                Toast.makeText(this, "Une erreur est survenue, merci de ressayer plus tard", Toast.LENGTH_LONG)
                    .show()
                finish()
            }
        ).addTo(viewDisposable)
    }
}
