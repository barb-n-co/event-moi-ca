package com.example.event_app.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView


class ScannerQrCodeActivity : BaseActivity(), ZXingScannerView.ResultHandler {

    private lateinit var mScannerView: ZXingScannerView

    companion object {
        const val QrCodeRequestCode = 10
        const val QrCodeKey = "QrCodeResponse"

        fun start(fromActivity: FragmentActivity) {
            fromActivity.startActivityForResult(
                Intent(fromActivity, ScannerQrCodeActivity::class.java), QrCodeRequestCode
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mScannerView = ZXingScannerView(this)
        setContentView(mScannerView)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun handleResult(result: Result?) {
        val resultIntent = Intent()
        resultIntent.putExtra(QrCodeKey, result?.text)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        mScannerView.setResultHandler(this)
        mScannerView.startCamera()
    }

    override fun onPause() {
        super.onPause()
        mScannerView.stopCamera()
    }
}
