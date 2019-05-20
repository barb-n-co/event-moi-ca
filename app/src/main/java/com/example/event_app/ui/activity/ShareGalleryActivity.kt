package com.example.event_app.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.event_app.R
import com.example.event_app.adapter.ListEventAdapter
import com.example.event_app.model.EventItem
import com.example.event_app.utils.GlideApp
import com.example.event_app.viewmodel.ShareGalleryViewModel
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_share_gallery.*
import org.kodein.di.generic.instance
import timber.log.Timber


class ShareGalleryActivity : BaseActivity() {

    private val viewModel: ShareGalleryViewModel by instance(arg = this)
    private var uri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share_gallery)

        uri = intent?.getParcelableExtra("uri") as Uri?

        viewModel.eventList.subscribe(
            {
                Timber.tag("TEST_").d(it.toString())
                initAdapter(it)
            },
            {
                Timber.tag("TEST_").d(it.toString())
                Timber.e(it)
            })
            .addTo(viewDisposable)

        viewModel.getMyEvents()

        if (uri != null && uri.toString().isNotEmpty()) {
            GlideApp.with(this).load(uri).into(iv_imageToShare)
            Timber.tag("success with photo").d("success: $uri")
        } else {
            Toast.makeText(
                this,
                "Une erreur est survenue, merci de ressayer plus tard",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }

    }

    private fun initAdapter(eventList: List<EventItem>) {
        val adapter = ListEventAdapter()
        val mLayoutManager = LinearLayoutManager(this)
        uri?.let {
            val galeryBitmap = viewModel.getBitmapWithResolver(this.contentResolver, it)

            rv_shareEvent.layoutManager = mLayoutManager
            rv_shareEvent.itemAnimator = DefaultItemAnimator()
            rv_shareEvent.adapter = adapter
            adapter.submitList(eventList)

            adapter.eventsClickPublisher.subscribe(
                {
                    viewModel.putImageWithBitmap(galeryBitmap, it, true)
                    Toast.makeText(this, "Votre image a été envoyé", Toast.LENGTH_LONG)
                        .show()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    finish()
                },
                {
                    Timber.e(it)
                    Toast.makeText(this, "Une erreur est survenue, merci de ressayer plus tard 2", Toast.LENGTH_LONG)
                        .show()
                    finish()
                }
            ).addTo(viewDisposable)
        }


    }
}
