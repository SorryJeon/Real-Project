package com.example.app.fakecarrotmarket.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.app.fakecarrotmarket.DataBase.GoguMarketDB
import com.example.app.fakecarrotmarket.R

class DBListViewAdapter(val context: Context, val goguMarketDB: ArrayList<GoguMarketDB>) :
    BaseAdapter() {
    @SuppressLint("ViewHolder", "SetTextI18n")

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        /* LayoutInflater는 item을 Adapter에서 사용할 View로 부풀려주는(inflate) 역할을 한다. */
        val view: View = LayoutInflater.from(context).inflate(R.layout.gogumarket_listview, null)

        /* 위에서 생성된 view를 gogumarket_listview.xml 파일의 각 View와 연결하는 과정이다. */
        val productImage = view.findViewById<ImageView>(R.id.productImg)
        val productName = view.findViewById<TextView>(R.id.productName)
        val productId = view.findViewById<TextView>(R.id.userAddress)
        val price = view.findViewById<TextView>(R.id.productPrice)

        /* ArrayList<GoguMarketDB>의 변수 productArray의 이미지와 데이터를 ImageView와 TextView에 담는다. */
        val productArray = goguMarketDB[position]
        val resourceId = context.resources.getIdentifier(
            productArray.productImgUrl,
            "drawable",
            context.packageName
        )
        productImage.setImageResource(resourceId)
        productName.text = productArray.productName
        productId.text = productArray.userAddress
        price.text = productArray.price.toString() + "원"

        return view
    }

    override fun getItem(position: Int): Any {
        return goguMarketDB[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return goguMarketDB.size
    }
}