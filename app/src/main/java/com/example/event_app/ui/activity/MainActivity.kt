package com.example.event_app.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.event_app.R
import com.example.event_app.viewmodel.MainActivityViewModel
import org.kodein.di.generic.instance

class MainActivity : BaseActivity() {

    private val viewModel : MainActivityViewModel by instance(arg = this)
    companion object {

        fun start(fromActivity: AppCompatActivity) {
            fromActivity.startActivity(
                Intent(fromActivity, MainActivity::class.java)
            )
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.toolbar))

        Toast.makeText(this, viewModel.getCurrentUserToDisplay(), Toast.LENGTH_LONG).show()
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
}




