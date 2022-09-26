package com.example.app.fakecarrotmarket

import android.content.ContentValues
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.ActionBar

class ProductActivity : AppCompatActivity() {

    private var back_Button: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product)

        val actionBar: ActionBar? = supportActionBar
        supportActionBar?.setDisplayShowTitleEnabled(false)
        actionBar?.hide()

        back_Button = findViewById<View>(R.id.backButton3) as Button

        back_Button!!.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onBackPressed() {
        Log.d(ContentValues.TAG, "상품 상세 정보 페이지에서 나가셨습니다.")
        super.onBackPressed()
    }
}