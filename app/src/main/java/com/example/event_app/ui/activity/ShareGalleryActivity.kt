package com.example.event_app.ui.activity

import android.content.Intent
import android.graphics.BitmapFactory
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

        if (SplashScreenActivity.sharedPhotoPath != null) {
            GlideApp.with(this).load(SplashScreenActivity.sharedPhotoPath).into(iv_imageToShare)
        } else if (uri != null && uri.toString().isNotEmpty()) {
            GlideApp.with(this).load(uri).into(iv_imageToShare)
        } else {
            Toast.makeText(
                this,
                getString(R.string.an_error_occured_please_try_later),
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }

    }

    private fun initAdapter(eventList: List<EventItem>) {
        val adapter = ListEventAdapter()

        rv_shareEvent.layoutManager = LinearLayoutManager(this)
        rv_shareEvent.itemAnimator = DefaultItemAnimator()
        rv_shareEvent.adapter = adapter
        adapter.submitList(eventList)

        uri?.let {

            val galeryBitmap = viewModel.getBitmapWithResolver(contentResolver, it)

            adapter.eventsClickPublisher.subscribe(
                {
                    viewModel.putImageWithBitmap(galeryBitmap, it, true)
                    Toast.makeText(this, getString(R.string.image_shared_success), Toast.LENGTH_LONG)
                        .show()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    finish()
                },
                {
                    Timber.e(it)
                    Toast.makeText(this, getString(R.string.an_error_occured_please_try_later), Toast.LENGTH_LONG)
                        .show()
                    finish()
                }
            ).addTo(viewDisposable)
        }

        SplashScreenActivity.sharedPhotoPath?.let { path ->

            adapter.eventsClickPublisher.subscribe(
                {
                    val bitmap = BitmapFactory.decodeFile(path)
                    viewModel.putImageWithBitmap(bitmap, it, true)
                    Toast.makeText(this, getString(R.string.image_shared_success), Toast.LENGTH_LONG)
                        .show()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    finish()
                },
                {
                    Timber.e(it)
                    Toast.makeText(this, getString(R.string.an_error_occured_please_try_later), Toast.LENGTH_LONG)
                        .show()
                    finish()
                }
            ).addTo(viewDisposable)

        }


    }
}
