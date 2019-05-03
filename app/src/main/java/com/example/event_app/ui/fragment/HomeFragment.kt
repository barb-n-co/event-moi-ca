package com.example.event_app.ui.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import com.example.event_app.R
import com.example.event_app.adapter.HomeViewPagerAdapter
import com.example.event_app.manager.PermissionManager
import com.example.event_app.ui.activity.ScannerQrCodeActivity
import kotlinx.android.synthetic.main.fragment_home.*
import com.example.event_app.viewmodel.HomeFragmentViewModel
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter
import org.kodein.di.generic.instance

class HomeFragment : BaseFragment() {

    private val viewModel : HomeFragmentViewModel by instance()

    companion object {
        const val TAG = "HOMEFRAGMENT"
        fun newInstance(): HomeFragment = HomeFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager()
        setFab()
    }

    private fun setFab() {
        fabmenu.setMenuListener(object : SimpleMenuListenerAdapter() {
            override fun onMenuItemSelected(menuItem: MenuItem?): Boolean =
                this@HomeFragment.onOptionsItemSelected(menuItem)
        })
    }

    private fun setupViewPager() {
        val adapter = HomeViewPagerAdapter(childFragmentManager)
        adapter.addFragment(InvitationFragment.newInstance(), getString(R.string.tb_invites))
        adapter.addFragment(MyEventsFragment.newInstance(), getString(R.string.tb_myevents))
        vp_saved_searches_history.adapter = adapter
        tl_invites_events_home_fragment.setupWithViewPager(vp_saved_searches_history)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_scan_qrcode -> {
                requestCameraPermission()
            }
            R.id.action_add_event -> {
                val action = HomeFragmentDirections.actionMyHomeFragmentToAddEventFragment()
                NavHostFragment.findNavController(this).navigate(action)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun openQrCode(){
        ScannerQrCodeActivity.start(activity!!)
    }

    fun requestCameraPermission(){
        if (permissionManager.requestCameraPermission(activity!!)) {
            openQrCode()
        }
    }

    override fun onResume() {
        super.onResume()
        setTitleToolbar(getString(R.string.title_home))
    }
}