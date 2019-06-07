package com.example.event_app.ui.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.event_app.R
import com.example.event_app.manager.PermissionManager
import com.example.event_app.repository.MyFirebaseMessagingService
import com.example.event_app.ui.fragment.*
import com.example.event_app.utils.or
import com.example.event_app.viewmodel.MainActivityViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
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

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        var returnValue = false

        when (item.itemId) {
            R.id.navigation_home -> {
                currentController = navControllerHome
                homeWrapper.visibility = View.VISIBLE
                profileWrapper.visibility = View.INVISIBLE
                eventMapWrapper.visibility = View.INVISIBLE
                app_bar.visibility = View.VISIBLE
                displayFilterMenu(true)
                supportActionBar?.setTitle(R.string.title_home)

                returnValue = true
            }
            R.id.navigation_profile -> {

                currentController = navControllerProfile

                homeWrapper.visibility = View.INVISIBLE
                profileWrapper.visibility = View.VISIBLE
                eventMapWrapper.visibility = View.INVISIBLE
                app_bar.visibility = View.VISIBLE
                displayFilterMenu(false)
                supportActionBar?.setTitle(R.string.title_profile)

                returnValue = true
            }
            R.id.navigation_event_map -> {

                currentController = navControllerEventMap

                eventMapWrapper.visibility = View.VISIBLE
                homeWrapper.visibility = View.INVISIBLE
                profileWrapper.visibility = View.INVISIBLE
                app_bar.visibility = View.VISIBLE
                displayFilterMenu(false)
                supportActionBar?.setTitle(R.string.title_event_map)

                val container = supportFragmentManager.findFragmentById(R.id.content_event_map)
                val frg = container?.childFragmentManager?.findFragmentById(R.id.content_event_map)
                if (frg is EventMapFragmentInterface) {
                    frg.displayMapItems()
                }
            }
        }
        return@OnNavigationItemSelectedListener returnValue
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        initView()

        currentController = navControllerHome
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    private fun initView() {
        navControllerHome = (supportFragmentManager
            .findFragmentById(R.id.content_home) as NavHostFragment)
            .navController

        navControllerProfile = (supportFragmentManager
            .findFragmentById(R.id.content_profile) as NavHostFragment)
            .navController

        navControllerEventMap = (supportFragmentManager
            .findFragmentById(R.id.content_event_map) as NavHostFragment)
            .navController

        homeWrapper = content_home_wrapper
        profileWrapper = content_profile_wrapper
        eventMapWrapper = content_event_map_wrapper
    }

    override fun supportNavigateUpTo(upIntent: Intent) {
        currentController.navigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.action_bar_menu, menu)
        filterButtonMenu = menu.findItem(R.id.action_filter)
        qrCodeButtonMenu = menu.findItem(R.id.action_qr_code)
        editEventButtonMenu = menu.findItem(R.id.action_edit_event)
        deleteEventButtonMenu = menu.findItem(R.id.action_delete_event)
        quitEventButtonMenu = menu.findItem(R.id.action_quit_event)
        downloadActionMenu = menu.findItem(R.id.action_download_photo)
        deletePhotoActionMenu = menu.findItem(R.id.action_delete_photo)
        reportPhotoActionMenu = menu.findItem(R.id.action_report)
        authorizePhotoActionMenu = menu.findItem(R.id.action_validate_photo)

        photoDetailActionList.add(downloadActionMenu)
        photoDetailActionList.add(deletePhotoActionMenu)
        photoDetailActionList.add(reportPhotoActionMenu)
        photoDetailActionList.add(authorizePhotoActionMenu)

        val container = supportFragmentManager.findFragmentById(R.id.content_home)
        val frg = container?.childFragmentManager?.findFragmentById(R.id.content_home)
        if (frg is HomeFragment) {
            displayFilterMenu(true)
        } else {
            displayFilterMenu(false)
        }
        displayQrCodeMenu(false)
        displayDetailPhotoActions(false)
        displayQuitEventMenu(false)
        displayDeleteEventMenu(false)
        displayEditEventMenu(false)
        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        val container = supportFragmentManager.findFragmentById(R.id.content_home)
        val frg = container?.childFragmentManager?.findFragmentById(R.id.content_home)

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
            R.id.action_qr_code -> {
                if (frg is DetailEventInterface) {
                    frg.loadQrCode()
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

    fun displayFilterMenu(value: Boolean) {
        filterButtonMenu?.isVisible = value
    }

    fun displayQrCodeMenu(value: Boolean) {
        qrCodeButtonMenu?.isVisible = value
    }

    fun displayEditEventMenu(value: Boolean) {
        editEventButtonMenu?.isVisible = value
    }

    fun displayDeleteEventMenu(value: Boolean) {
        deleteEventButtonMenu?.isVisible = value
    }

    fun displayQuitEventMenu(value: Boolean) {
        quitEventButtonMenu?.isVisible = value
    }

    fun displayLoader(value: Boolean) {
        pb_main_activity.visibility = if(value) View.VISIBLE else View.INVISIBLE
    }

    override fun onSupportNavigateUp(): Boolean {
        currentController.navigateUp()
        return true
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
                    Timber.w(task.exception, "getInstanceId failed%s")
                    return@addOnCompleteListener
                }

                // Get new Instance ID token
                val token = task.result?.token

                Timber.d(token)
                token?.let {
                    MyFirebaseMessagingService.token = token
                }

            }

        val container = supportFragmentManager.findFragmentById(R.id.content_home)
        val frg = container?.childFragmentManager?.findFragmentById(R.id.content_home)
        if(frg is HomeFragment){
            displayLoader(true)
        }
    }

    override fun onBackPressed() {
        if (!isMapOpenned) {
            currentController
                .let { if (it.popBackStack().not()) finish() }
                .or { finish() }
        } else {
            AddAddressMapFragment.popBack()
            isMapOpenned = false
        }

    }

    private fun openQrCode() {
        ScannerQrCodeActivity.start(this)
    }

    fun isMapOpen(value: Boolean) {
        isMapOpenned = value
    }

}




