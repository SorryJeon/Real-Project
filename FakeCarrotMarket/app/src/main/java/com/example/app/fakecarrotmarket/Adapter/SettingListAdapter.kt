package com.example.app.fakecarrotmarket.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.app.fakecarrotmarket.DataBase.SettingListView
import com.example.app.fakecarrotmarket.R

class SettingListAdapter(val context: Context, val menuList: ArrayList<SettingListView>) :
    BaseAdapter() {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val view: View = LayoutInflater.from(context).inflate(R.layout.setting_listview, null)
        val settingMenu = view.findViewById<TextView>(R.id.settingMenu)

        val menu = menuList[position]
        settingMenu.text = menu.settingMenu

        return view
    }

    override fun getItem(position: Int): Any {
        return menuList[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return menuList.size
    }
}