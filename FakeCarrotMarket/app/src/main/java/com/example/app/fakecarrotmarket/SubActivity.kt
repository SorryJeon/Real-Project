package com.example.app.fakecarrotmarket

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.ActionBar
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_sub.*

private lateinit var homeFragment: HomeFragment
private lateinit var chatFragment: ChatFragment
private lateinit var settingFragment: SettingFragment

class SubActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sub)

        val actionBar : ActionBar? = supportActionBar
        actionBar?.hide()

        bottomNavigationView.setOnNavigationItemSelectedListener(BottomNavItemSelectedListener)
        homeFragment = HomeFragment.newInstance()
        supportFragmentManager.beginTransaction().add(R.id.linearLayout, homeFragment).commit()
    }

    private val BottomNavItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener{
        when(it.itemId){
            R.id.page_home -> {
                homeFragment = HomeFragment.newInstance()
                supportFragmentManager.beginTransaction().replace(R.id.linearLayout, homeFragment).commit()
            }
            R.id.page_chat -> {
                chatFragment = ChatFragment.newInstance()
                supportFragmentManager.beginTransaction().replace(R.id.linearLayout, chatFragment).commit()
            }
            R.id.page_setting -> {
                settingFragment = SettingFragment.newInstance()
                supportFragmentManager.beginTransaction().replace(R.id.linearLayout, settingFragment).commit()
            }
        }
        true
    }
}