package com.example.event_app.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.event_app.R
import com.example.event_app.adapter.HomeViewPagerAdapter
import kotlinx.android.synthetic.main.fragment_home.*
import com.example.event_app.viewmodel.HomeFragmentViewModel
import org.kodein.di.generic.instance

class HomeFragment : BaseFragment() {

    private val viewModel: HomeFragmentViewModel by instance()

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
/*
        fab_fragment_home.setOnClickListener {

        }
        */
    }

    private fun setupViewPager() {
        val adapter = HomeViewPagerAdapter(childFragmentManager)
        adapter.addFragment(InvitationFragment.newInstance(), getString(R.string.tb_invites))
        adapter.addFragment(MyEventsFragment.newInstance(), getString(R.string.tb_myevents))
        vp_saved_searches_history.adapter = adapter
        tl_invites_events_home_fragment.setupWithViewPager(vp_saved_searches_history)
    }

    override fun onResume() {
        super.onResume()
        setTitleToolbar(getString(R.string.title_home))
    }
}