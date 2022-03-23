package com.example.app.fakecarrotmarket.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.example.app.fakecarrotmarket.DataBase.ListViewItem
import com.example.app.fakecarrotmarket.R
import kotlinx.android.synthetic.main.custom_listview.view.*

class ListViewAdapter(private val items: MutableList<ListViewItem>) : BaseAdapter() {
    override fun getCount(): Int = items.size
    override fun getItem(position: Int): ListViewItem = items[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(
        position: Int,
        view: View?,
        parent: ViewGroup?
    ): View {
        var convertView = view
        if (convertView == null) convertView =
            LayoutInflater.from(parent?.context).inflate(R.layout.custom_listview, parent, false)
        val item: ListViewItem = items[position]
        convertView!!.image_title.setImageDrawable(item.icon)
        convertView.text_title.text = item.title
        convertView.text_sub_title.text = item.subTitle
        return convertView
    }

    fun add(key: String?) {

    }

    fun remove(key: String?) {

    }
}