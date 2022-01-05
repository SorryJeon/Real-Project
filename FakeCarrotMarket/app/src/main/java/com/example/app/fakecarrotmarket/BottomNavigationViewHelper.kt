package com.example.app.fakecarrotmarket

import android.content.Context
import android.content.Intent
import com.google.android.material.bottomnavigation.BottomNavigationView


object BottomNavigationViewHelper {
    fun enableNavigation(context: Context, view: BottomNavigationView) {
        view.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.page_home -> {
                    val intent1 = Intent(context, MainActivity::class.java) // 0
                    context.startActivity(intent1)
                }
                R.id.page_chat -> {
                    val intent2 = Intent(context, ChatActivity::class.java) // 1
                    context.startActivity(intent2)
                }
                R.id.page_setting -> {
                    val intent3 = Intent(context, SettingActivity::class.java) // 2
                    context.startActivity(intent3)
                }
            }
            false
        }
    }
}