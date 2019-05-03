package com.example.event_app.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.example.event_app.R
import com.example.event_app.manager.PermissionManager
import com.example.event_app.viewmodel.MainActivityViewModel
import org.kodein.di.generic.instance
import timber.log.Timber

class MainActivity : BaseActivity() {

    private val viewModel: MainActivityViewModel by instance(arg = this)

    companion object {

        fun start(fromActivity: FragmentActivity) {
            fromActivity.startActivity(
                Intent(fromActivity, MainActivity::class.java)
            )
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.toolbar))

        viewModel.user.subscribe(
            {
                Toast.makeText(this, getString(R.string.toast_welcome_user_main_activity, it.name), Toast.LENGTH_LONG)
                    .show()
            },
            {
                Timber.e(it)
            }
        ).dispose()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.action_bar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_logout -> {
            viewModel.logout()
            Toast.makeText(this, "sign out", Toast.LENGTH_SHORT).show()
            LoginActivity.start(this)
            finish()
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionManager.REQUEST_PERMISSION_CAMERA && grantResults[permissions.indexOf(Manifest.permission.CAMERA)] == PackageManager.PERMISSION_GRANTED) {
            openQrCode()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        super.onBackPressed()
        return true
    }

    fun openQrCode() {
        ScannerQrCodeActivity.start(this)
    }
}




