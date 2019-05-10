package com.example.event_app.ui.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.SearchView
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.event_app.R
import com.example.event_app.manager.PermissionManager
import com.example.event_app.ui.fragment.HomeInterface
import com.example.event_app.utils.or
import com.example.event_app.viewmodel.MainActivityViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*
import org.kodein.di.generic.instance
import timber.log.Timber

class MainActivity : BaseActivity() {

    private val viewModel: MainActivityViewModel by instance(arg = this)
    var isFromMap = false
    private var searchBtn: MenuItem? = null
    private var logoutBtn: MenuItem? = null


    private lateinit var currentController: NavController
    private lateinit var navControllerHome: NavController
    private lateinit var navControllerProfile: NavController

    private lateinit var homeWrapper: FrameLayout
    private lateinit var profileWrapper: FrameLayout

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
                app_bar.visibility = View.VISIBLE
                supportActionBar?.setTitle(R.string.title_home)

                returnValue = true
            }
            R.id.navigation_profile -> {

                currentController = navControllerProfile

                homeWrapper.visibility = View.INVISIBLE
                profileWrapper.visibility = View.VISIBLE
                app_bar.visibility = View.VISIBLE
                supportActionBar?.setTitle(R.string.title_profile)

                returnValue = true
            }
        }
        return@OnNavigationItemSelectedListener returnValue
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        initView()

        viewModel.user.subscribe(
            {
                Toast.makeText(this, getString(R.string.toast_welcome_user_main_activity, it.name), Toast.LENGTH_LONG)
                    .show()
            },
            {
                Timber.e(it)
            }
        ).dispose()

        currentController = navControllerHome
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    private fun initView(){
        navControllerHome = (supportFragmentManager
            .findFragmentById(R.id.content_home) as NavHostFragment)
            .navController

        navControllerProfile = (supportFragmentManager
            .findFragmentById(R.id.content_profile) as NavHostFragment)
            .navController

        homeWrapper = content_home_wrapper
        profileWrapper = content_profile_wrapper
    }

    override fun supportNavigateUpTo(upIntent: Intent) {
        currentController.navigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_maps, menu)
        menuInflater.inflate(R.menu.action_bar_menu, menu)

       searchBtn = menu?.findItem(R.id.sv_search_map)
       logoutBtn = menu?.findItem(R.id.action_filter)
        displaySearchButton(false)
        displayLogoutButton(false)



       if (isFromMap){
           displaySearchButton(true)
           displayLogoutButton(false)
           setSearchView(menu)
       }

       else{
           isFromMap = !isFromMap
           displaySearchButton(false)
           displayLogoutButton(true)

       }
        return super.onCreateOptionsMenu(menu)
    }

    fun displaySearchButton(value: Boolean) {
        searchBtn?.isVisible = value
    }

    fun displayLogoutButton(value: Boolean) {
        logoutBtn?.isVisible = value
    }

    private fun setSearchView(menu: Menu) {
        val searchView = menu.findItem(R.id.sv_search_map).actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {

                query?.let {
                    viewModel.searchAdress(query)
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                return false
            }
        })
    }

    fun updateToolbar(isfrommap: Boolean) {
        isFromMap = isfrommap
        setSupportActionBar(findViewById(R.id.toolbar))

    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_filter -> {
            val container = supportFragmentManager.findFragmentById(R.id.content_home)
            val frg = container?.childFragmentManager?.findFragmentById(R.id.content_home)
            if (frg is HomeInterface) {
                frg.openFilter()
            }
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
        currentController.navigateUp()
        return true
    }

    override fun onBackPressed() {
        currentController
            .let { if (it.popBackStack().not()) finish() }
            .or { finish ()}
    }

    private fun openQrCode() {
        ScannerQrCodeActivity.start(this)
    }


}




