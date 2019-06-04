package com.example.event_app.ui.activity

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.event_app.R
import com.example.event_app.manager.PermissionManager
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import kotlinx.android.synthetic.main.activity_generation_qrcode.*


class GenerationQrCodeActivity : BaseActivity() {

    private val QRcodeWidth = 500
    private val IMAGE_DIRECTORY = "/QRcodeDemonuts"
    var bitmap: Bitmap? = null

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
        setSupportActionBar(toolbar_qrcode)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val idCard = intent.getStringExtra(ExtraCardId)

        try {
            bitmap = encodeAsBitmap(idCard)
            iv_qr_code.setImageBitmap(bitmap)

        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_qr_code, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_share -> {
            if (permissionManager.checkPermissions(
                    arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                )
            ) {
                share()
            } else {
                requestPermissions()
            }

            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    private fun share() {
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
        startActivity(Intent.createChooser(intent, "Share"))
    }

    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        permissionManager.requestPermissions(permissions, PermissionManager.PERMISSION_IMPORT, this)
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
