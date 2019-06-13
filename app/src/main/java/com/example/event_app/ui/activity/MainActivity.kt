package com.example.event_app.ui.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.event_app.R
import com.example.event_app.manager.PermissionManager
import com.example.event_app.repository.MyFirebaseMessagingService
import com.example.event_app.ui.fragment.HomeInterface
import com.example.event_app.viewmodel.MainActivityViewModel
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_main.*
import org.kodein.di.generic.instance
import timber.log.Timber


class MainActivity : BaseActivity() {

    private val viewModel: MainActivityViewModel by instance(arg = this)

    private var isMapOpenned = false


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
        setupNavigation()
        FirebaseDynamicLinks.getInstance().getDynamicLink(intent)
            .addOnSuccessListener {
                if(it != null) {
                    it.link.let {
                        it.getQueryParameter("param")?.let {
                            addInvitation(it)
                        }
                    }
                }
            }
    }

    private fun setupNavigation() {
        val navController = NavHostFragment.findNavController(parent_host_fragment)
        navigation.setupWithNavController(navController)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ScannerQrCodeActivity.QrCodeRequestCode -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.getStringExtra(ScannerQrCodeActivity.QrCodeKey)?.let {
                        Uri.parse(it).getQueryParameter("param")?.let {
                            addInvitation(it)
                        }
                    }
                }
            }
        }
    }

    private fun addInvitation(idEvent: String){
        val container = supportFragmentManager.findFragmentById(R.id.parent_host_fragment)
        val frg = container?.childFragmentManager?.findFragmentById(R.id.parent_host_fragment)
        if (frg is HomeInterface) {
            frg.getInvitation(idEvent)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionManager.REQUEST_PERMISSION_CAMERA && grantResults[permissions.indexOf(Manifest.permission.CAMERA)] == PackageManager.PERMISSION_GRANTED) {
            openQrCode()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.sentNewToken()
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Timber.w("getInstanceId failed%s", task.exception.toString())
                    return@addOnCompleteListener
                }

                // Get new Instance ID token
                val token = task.result?.token

                Timber.d(token)
                token?.let {
                    MyFirebaseMessagingService.token = token
                }

            }
    }

    private fun openQrCode() {
        ScannerQrCodeActivity.start(this)
    }

    fun isMapOpen(value: Boolean) {
        isMapOpenned = value
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        if (supportFragmentManager.primaryNavigationFragment?.childFragmentManager?.backStackEntryCount ?: 0 > 0) {
            NavHostFragment.findNavController(parent_host_fragment).popBackStack()
        } else {
            super.onBackPressed()
        }
    }
}




