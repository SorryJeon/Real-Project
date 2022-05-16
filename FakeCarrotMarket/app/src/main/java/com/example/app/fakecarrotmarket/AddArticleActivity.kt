package com.example.app.fakecarrotmarket

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class AddArticleActivity : AppCompatActivity() {

    var btnimage: Button? = null
    var btnupload: Button? = null
    var btndb: Button? = null
    var superbtn: Button? = null
    var auth: FirebaseAuth? = null
    var iv: ImageView? = null
    var titleEditText: EditText? = null
    var priceEditText: EditText? = null
    var titleType: Button? = null
    var tvafter: TextView? = null
    var productType: TextView? = null
    var imgUri: Uri? = null
    var imgUrl: String = ""
    var fbStorage: FirebaseStorage? = null
    var fbFireStore: FirebaseFirestore? = null
    var nickname: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_article)

        btnimage = findViewById<View>(R.id.btn_image) as Button
        btnupload = findViewById<View>(R.id.btn_upload) as Button
        btndb = findViewById<View>(R.id.btn_db) as Button
        superbtn = findViewById<View>(R.id.btn_superUpload) as Button
        iv = findViewById<View>(R.id.iv) as ImageView
        titleEditText = findViewById<View>(R.id.titleEditText) as EditText
        priceEditText = findViewById<View>(R.id.priceEditText) as EditText
        titleType = findViewById<View>(R.id.titleType) as Button
        tvafter = findViewById<View>(R.id.tv_after) as TextView
        productType = findViewById<View>(R.id.product_type) as TextView
        nickname = findViewById<View>(R.id.nickname) as TextView

        val actionBar: ActionBar? = supportActionBar
        actionBar?.hide()
    }
}