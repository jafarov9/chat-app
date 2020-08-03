package com.ntech.chatapp.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.ntech.chatapp.R
import com.ntech.chatapp.ui.adapter.ViewPagerAdapter
import com.ntech.chatapp.ui.fragment.ChatsFragment
import com.ntech.chatapp.ui.fragment.FriendsFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupUI()
    }

    private fun setupUI() {
        setSupportActionBar(mainToolbar)
        setupViewPager()
        main_tabs.setupWithViewPager(main_view_pager)

    }

    private fun setupViewPager() {

        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.apply {
            addFragment(ChatsFragment(), "Messages")
            addFragment(FriendsFragment(), "Friends")
        }

        main_view_pager.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.action_log_out -> {
                mAuth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            R.id.action_profile -> {
                startActivity(Intent(this, ProfileActivity::class.java))
            }
        }
        return true
    }
}