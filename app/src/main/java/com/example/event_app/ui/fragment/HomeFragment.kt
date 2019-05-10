package com.example.event_app.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.example.event_app.R
import com.example.event_app.adapter.HomeViewPagerAdapter
import com.example.event_app.ui.activity.ScannerQrCodeActivity
import com.example.event_app.viewmodel.HomeFragmentViewModel
import kotlinx.android.synthetic.main.fragment_home.*
import org.kodein.di.generic.instance

class HomeFragment : BaseFragment(), HomeInterface {


    private val viewModel : HomeFragmentViewModel by instance(arg = this)

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
        setDisplayHomeAsUpEnabled(false)
        setupViewPager()
        setFab()
    }

    private fun setFab() {
        fabmenu_home.addOnMenuItemClickListener { miniFab, label, itemId ->
            when (itemId) {
                R.id.action_scan_qrcode -> {
                    requestCameraPermission()
                }
                R.id.action_add_event -> {
                    val action = HomeFragmentDirections.actionMyHomeFragmentToAddEventFragment(getString(R.string.chip_adresse))
                    NavHostFragment.findNavController(this).navigate(action)
                }
            }
        }
    }

    private fun setupViewPager() {
        val adapter = HomeViewPagerAdapter(childFragmentManager)
        adapter.addFragment(InvitationFragment.newInstance(), getString(R.string.tb_invites))
        adapter.addFragment(MyEventsFragment.newInstance(), getString(R.string.tb_myevents))
        vp_saved_searches_history.adapter = adapter
        tl_invites_events_home_fragment.setupWithViewPager(vp_saved_searches_history)
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

    override fun getInvitation(idEvent: String) {
        viewModel.addInvitation(idEvent)
    }
}

interface HomeInterface {
    fun getInvitation(idEvent: String)
}