package com.example.event_app.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.event_app.R
import com.example.event_app.repository.UserRepository
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment: BaseFragment() {

    private lateinit var userRepository : UserRepository

    companion object {
        const val TAG = "HOMEFRAGMENT"
        fun newInstance(): HomeFragment = HomeFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userRepository = UserRepository.getInstance(context!!)
        logoutButton.setOnClickListener {
            userRepository.fireBaseAuth.signOut()
            Toast.makeText(context, "sign out", Toast.LENGTH_SHORT).show()
        }

    }

}