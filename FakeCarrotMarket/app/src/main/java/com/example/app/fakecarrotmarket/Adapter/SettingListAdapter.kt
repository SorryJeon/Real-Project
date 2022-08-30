package com.example.app.fakecarrotmarket.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.app.fakecarrotmarket.DataBase.SettingListView
import com.example.app.fakecarrotmarket.R

class SettingListAdapter(val context: Context, val menuList: ArrayList<SettingListView>) :
    RecyclerView.Adapter<SettingListAdapter.ItemViewHolder>() {
    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val settingMenu = itemView.findViewById<TextView>(R.id.settingMenu)

        fun bind(settingListView: SettingListView, context: Context) {
            settingMenu.text = settingListView.settingMenu
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.setting_listview, null)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(menuList[position], context)
    }

    override fun getItemCount(): Int {
        return menuList.size
    }
}

//    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
//
//        val view: View = LayoutInflater.from(context).inflate(R.layout.setting_listview, null)
//        val settingMenu = view.findViewById<TextView>(R.id.settingMenu)
//
//        val menu = menuList[position]
//        settingMenu.text = menu.settingMenu
//
//        return view
//    }
//
//    override fun getItem(position: Int): Any {
//        return menuList[position]
//    }
//
//    override fun getItemId(position: Int): Long {
//        return 0
//    }
//
//    override fun getCount(): Int {
//        return menuList.size
//    }