package com.example.event_app.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import com.example.event_app.R
import com.example.event_app.utils.GlideApp
import com.example.event_app.viewmodel.ShareGalleryViewModel
import kotlinx.android.synthetic.main.activity_share_gallery.*
import org.kodein.di.generic.instance


class ShareGalleryActivity : BaseActivity() {

    private val viewModel: ShareGalleryViewModel by instance(arg = this)

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
                        val imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM) as Uri
                        if (imageUri != null) {
                            GlideApp.with(this).load(imageUri).into(iv_imageToShare)
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
}
