package com.example.event_app.ui.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.FrameLayout
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.event_app.R
import com.example.event_app.manager.PermissionManager
import com.example.event_app.repository.MyFirebaseMessagingService
import com.example.event_app.ui.fragment.DetailEventInterface
import com.example.event_app.ui.fragment.DetailPhotoInterface
import com.example.event_app.ui.fragment.HomeFragment
import com.example.event_app.ui.fragment.HomeInterface
import com.example.event_app.viewmodel.MainActivityViewModel
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_main.*
import org.kodein.di.generic.instance
import timber.log.Timber


class MainActivity : BaseActivity() {

    private var authorizePhotoActionMenu: MenuItem? = null
    private var reportPhotoActionMenu: MenuItem? = null
    private var deletePhotoActionMenu: MenuItem? = null
    private var downloadActionMenu: MenuItem? = null
    private val photoDetailActionList = mutableListOf<MenuItem?>()
    private val viewModel: MainActivityViewModel by instance(arg = this)
    private var filterButtonMenu: MenuItem? = null
    private var qrCodeButtonMenu: MenuItem? = null
    private var editEventButtonMenu: MenuItem? = null
    private var deleteEventButtonMenu: MenuItem? = null
    private var quitEventButtonMenu: MenuItem? = null
    private var downloadPicturesButtonMenu: MenuItem? = null
    private var searchEventButtonMenu: MenuItem? = null

    private lateinit var currentController: NavController
    private lateinit var navControllerHome: NavController
    private lateinit var navControllerProfile: NavController
    private lateinit var navControllerEventMap: NavController
    private lateinit var homeWrapper: FrameLayout
    private lateinit var profileWrapper: FrameLayout
    private lateinit var eventMapWrapper: FrameLayout
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
        setSupportActionBar(toolbar)
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.action_bar_menu, menu)
        filterButtonMenu = menu.findItem(R.id.action_filter)
        qrCodeButtonMenu = menu.findItem(R.id.action_invitation)
        editEventButtonMenu = menu.findItem(R.id.action_edit_event)
        deleteEventButtonMenu = menu.findItem(R.id.action_delete_event)
        quitEventButtonMenu = menu.findItem(R.id.action_quit_event)
        downloadActionMenu = menu.findItem(R.id.action_download_photo)
        deletePhotoActionMenu = menu.findItem(R.id.action_delete_photo)
        reportPhotoActionMenu = menu.findItem(R.id.action_report)
        authorizePhotoActionMenu = menu.findItem(R.id.action_validate_photo)
        downloadPicturesButtonMenu = menu.findItem(R.id.action_download_every_photos)
        searchEventButtonMenu = menu.findItem(R.id.sv_search_event)

        photoDetailActionList.add(downloadActionMenu)
        photoDetailActionList.add(deletePhotoActionMenu)
        photoDetailActionList.add(reportPhotoActionMenu)
        photoDetailActionList.add(authorizePhotoActionMenu)

        val container = supportFragmentManager.findFragmentById(R.id.parent_host_fragment)
        val frg = container?.childFragmentManager?.findFragmentById(R.id.parent_host_fragment)
        if (frg is HomeFragment) {
            displayFilterMenu(true)
            displaySearchEventMenu(true)
        } else {
            displayFilterMenu(false)
            displaySearchEventMenu(false)
        }
        displayDetailPhotoActions(false)
        displayQuitEventMenu(false)
        displayDeleteEventMenu(false)
        displayEditEventMenu(false)
        displayDownloadPicturesMenu(false)
        setSearchView(menu, frg)
        return super.onCreateOptionsMenu(menu)
    }

    private fun setSearchView(menu: Menu, frg: Fragment?) {
        val searchView = menu.findItem(R.id.sv_search_event).actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    if (frg is HomeInterface) {
                        frg.searchEvent(it)
                    }
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    if (frg is HomeInterface) {
                        frg.searchEvent(it)
                    }
                }
                return false
            }
        })
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        val container = supportFragmentManager.findFragmentById(R.id.parent_host_fragment)
        val frg = container?.childFragmentManager?.findFragmentById(R.id.parent_host_fragment)

        return when (item?.itemId) {
            R.id.action_filter -> {
                if (frg is HomeInterface) {
                    frg.openFilter()
                }
                true
            }
            R.id.action_report -> {
                if (frg is DetailPhotoInterface) {
                    frg.reportAction()
                }
                true
            }
            R.id.action_delete_photo -> {
                if (frg is DetailPhotoInterface) {
                    frg.deleteAction()
                }
                true
            }
            R.id.action_download_photo -> {
                if (frg is DetailPhotoInterface) {
                    frg.downloadAction()
                }
                true
            }
            R.id.action_validate_photo -> {
                if (frg is DetailPhotoInterface) {
                    frg.authorizeAction()
                }
                true
            }
            R.id.action_download_every_photos -> {
                if (frg is DetailEventInterface) {
                    frg.downloadPictures()
                }
                true
            }
            R.id.action_edit_event -> {
                if (frg is DetailEventInterface) {
                    frg.editEvent()
                }
                true
            }
            R.id.action_delete_event -> {
                if (frg is DetailEventInterface) {
                    frg.deleteEvent()
                }
                true
            }
            R.id.action_quit_event -> {
                if (frg is DetailEventInterface) {
                    frg.quitEvent()
                }
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ScannerQrCodeActivity.QrCodeRequestCode -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.getStringExtra(ScannerQrCodeActivity.QrCodeKey)?.let {
                        addInvitation(it)
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

    fun displayFilterMenu(value: Boolean) {
        filterButtonMenu?.isVisible = value
    }

    fun displaySearchEventMenu(value: Boolean) {
        searchEventButtonMenu?.isVisible = value
    }

    fun displayEditEventMenu(value: Boolean) {
        editEventButtonMenu?.isVisible = value
    }

    fun displayDownloadPicturesMenu(value: Boolean) {
        downloadPicturesButtonMenu?.isVisible = value
    }

    fun displayDeleteEventMenu(value: Boolean) {
        deleteEventButtonMenu?.isVisible = value
    }

    fun displayQuitEventMenu(value: Boolean) {
        quitEventButtonMenu?.isVisible = value
    }

    fun displayDetailPhotoActions(value: Boolean) {
        photoDetailActionList.forEach {
            it?.isVisible = value
        }
    }

    fun displayDetailPhotoActionValidatePhoto(value: Boolean) {
        authorizePhotoActionMenu?.isVisible = value
    }

    fun displayDetailPhotoActionDeletePhoto(value: Boolean) {
        deletePhotoActionMenu?.isVisible = value
    }

    fun displayDetailPhotoMenuRestricted(value: Boolean) {
        downloadActionMenu?.isVisible = value
        reportPhotoActionMenu?.isVisible = value
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




