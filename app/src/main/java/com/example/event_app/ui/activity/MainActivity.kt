package com.example.event_app.ui.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import com.example.event_app.R
import com.example.event_app.manager.PermissionManager
import com.example.event_app.ui.fragment.HomeInterface
import com.example.event_app.viewmodel.MainActivityViewModel
import org.kodein.di.generic.instance
import timber.log.Timber

class MainActivity : BaseActivity() {

    private val viewModel: MainActivityViewModel by instance(arg = this)
            var isFromMap = false

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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (isFromMap){
            menuInflater.inflate(R.menu.menu_maps, menu)
            setSearchView(menu)
        }

        else{
            isFromMap = !isFromMap
            menuInflater.inflate(R.menu.action_bar_menu, menu)

        }
        return super.onCreateOptionsMenu(menu)
    }

    private fun setSearchView(menu: Menu) {
        val searchView = menu.findItem(R.id.sv_search_map).actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {

                query?.let {
                    viewModel.searchAdress(query)
                    //                    viewModel.action(NannyParentsAction.SearchAddress(it))
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                return false
            }
        })
    }
    fun updateToolbar(isfrommap : Boolean){
        isFromMap = isfrommap
        setSupportActionBar(findViewById(R.id.toolbar))

    }
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_logout -> {
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle(R.string.tv_title_dialog_logout)
                .setMessage(R.string.tv_message_dialog_logout)
                .setNegativeButton(R.string.b_cancel_dialog_logout) { dialoginterface, i -> }
                .setPositiveButton(R.string.b_validate_dialog_logout) { dialoginterface, i ->
                    viewModel.logout()
                    Toast.makeText(this, getString(R.string.t_sign_out), Toast.LENGTH_SHORT).show()
                    LoginActivity.start(this)
                    finish()
                }.show()
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ScannerQrCodeActivity.QrCodeRequestCode -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.getStringExtra(ScannerQrCodeActivity.QrCodeKey)?.let {
                        val container = supportFragmentManager.findFragmentById(R.id.content_home)
                        val frg = container?.childFragmentManager?.findFragmentById(R.id.content_home)
                        if (frg is HomeInterface) {
                            frg.getInvitation(it)
                        }
                    }
                }
            }
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

    private fun openQrCode() {
        ScannerQrCodeActivity.start(this)
    }



}




