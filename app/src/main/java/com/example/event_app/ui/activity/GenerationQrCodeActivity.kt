package com.example.event_app.ui.activity

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.event_app.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import kotlinx.android.synthetic.main.activity_generation_qrcode.*


class GenerationQrCodeActivity : BaseActivity() {

    private val QRcodeWidth = 500
    private var bitmap: Bitmap? = null
    private var link: String? = null

    companion object {
        const val ExtraCardId = "ExtraCardId"
        fun start(activity: AppCompatActivity, cardId: String) =
            activity.startActivity(Intent(activity, GenerationQrCodeActivity::class.java).apply {
                putExtra(ExtraCardId, cardId)
            })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generation_qrcode)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val idCard = intent.getStringExtra(ExtraCardId)

        val deepLink = "https://eventmoica.page.link/invitation?param=${idCard}"

        val builder = FirebaseDynamicLinks.getInstance()
            .createDynamicLink()
            .setDomainUriPrefix("https://eventmoica.page.link")
            .setAndroidParameters(DynamicLink.AndroidParameters.Builder().build())
            .setLink(Uri.parse(deepLink))

        link = builder.buildDynamicLink().uri.toString()

        link?.let {
            initActions(it)
            try {
                bitmap = encodeAsBitmap(it)
                iv_qr_code.setImageBitmap(bitmap)
                b_share_qrcode_event_qrcode_activity.isEnabled = true
            } catch (e: WriterException) {
                e.printStackTrace()
            }
        }
    }

    private fun initActions(link: String) {
        b_copy_link_event_qrcode_activity.isEnabled = true
        b_share_link_event_qrcode_activity.isEnabled = true

        b_copy_link_event_qrcode_activity.setOnClickListener {
            copyLink(link)
        }
        b_share_qrcode_event_qrcode_activity.setOnClickListener {
            permissionManager.executeFunctionWithPermissionNeeded(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                { shareQrCode() })
        }
        b_share_link_event_qrcode_activity.setOnClickListener {
            shareLink(link)
        }
    }

    private fun copyLink(link: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(getString(R.string.title_copy_qrcode_activity), link)
        clipboard.primaryClip = clip
        Snackbar.make(
            cl_generation_qrcode_activity,
            getString(R.string.tv_link_copied_qrcode_activity),
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun shareLink(link: String) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.title_share_link_qrcode_activity))
        val shareMessage = getString(R.string.message_share_link_qrcode_activity, link)
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
        startActivity(Intent.createChooser(shareIntent, getString(R.string.title_share_intent_qrcode_activity)))
    }

    private fun shareQrCode() {
        val bitmapPath = MediaStore.Images.Media.insertImage(
            contentResolver,
            bitmap,
            "Invitation",
            null
        )
        val bitmapUri = Uri.parse(bitmapPath)
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "image/png"
        intent.putExtra(Intent.EXTRA_STREAM, bitmapUri)
        startActivity(Intent.createChooser(intent, getString(R.string.title_share_intent_qrcode_activity)))
    }

    private fun encodeAsBitmap(str: String): Bitmap? {
        val result: BitMatrix
        try {
            result = MultiFormatWriter().encode(
                str,
                BarcodeFormat.QR_CODE, QRcodeWidth, QRcodeWidth, null
            )
        } catch (iae: IllegalArgumentException) {
            // Unsupported format
            return null
        }

        val w = result.width
        val h = result.height
        val pixels = IntArray(w * h)
        for (y in 0 until h) {
            val offset = y * w
            for (x in 0 until w) {
                pixels[offset + x] =
                    if (result.get(x, y)) ContextCompat.getColor(this, R.color.black) else ContextCompat.getColor(
                        this,
                        R.color.white
                    )
            }
        }
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, QRcodeWidth, 0, 0, w, h)
        return bitmap
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
