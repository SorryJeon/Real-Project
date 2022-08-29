package com.example.app.fakecarrotmarket

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.example.app.fakecarrotmarket.Adapter.SettingListAdapter
import com.example.app.fakecarrotmarket.DataBase.SettingListView
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.kakao.auth.Session
import com.twitter.sdk.android.core.TwitterCore

class SettingActivity : AppCompatActivity() {

    val TAG: String = "안녕"
    private var temp: String? = null
    var first_time: Long = 0
    var second_time: Long = 0
    var iv: ImageView? = null
    var tvafter: TextView? = null
    var inputId: TextView? = null
    var inputName: TextView? = null
    var auth: FirebaseAuth? = null
    var btntoken: Button? = null
    var btnRevoke: Button? = null
    var btnLogout: Button? = null
    var fbStorage: FirebaseStorage? = null
    var fbFireStore: FirebaseFirestore? = null
    private lateinit var googleSignInClient: GoogleSignInClient

    private var firebaseDatabase = FirebaseDatabase.getInstance()
    private var databaseReference = firebaseDatabase.reference

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        btnLogout = findViewById<View>(R.id.btn_logout) as Button
        btnRevoke = findViewById<View>(R.id.btn_revoke) as Button
        btntoken = findViewById<View>(R.id.btn_token) as Button
        tvafter = findViewById<View>(R.id.tv_after) as TextView
        inputId = findViewById<View>(R.id.input_id) as TextView
        inputName = findViewById<View>(R.id.input_name) as TextView
        iv = findViewById<View>(R.id.iv) as ImageView
        auth = FirebaseAuth.getInstance()
        fbStorage = FirebaseStorage.getInstance()
        fbFireStore = FirebaseFirestore.getInstance()

        val actionBar: ActionBar? = supportActionBar
        actionBar?.title = "유저목록"

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val sharedPreference = getSharedPreferences("temp record", MODE_PRIVATE)
        val savedTemp = sharedPreference.getString("temp", "")
        temp = savedTemp.toString()
        if (temp != "") {
            Log.d(ContentValues.TAG, "현재 접속중인 무작위 회원 : 고구마켓$temp")
        }

        val tempName = "고구마켓$temp"
        inputName!!.text = "무작위 닉네임 : $tempName"
        inputId!!.text = "현재 접속중인 ID : ${auth?.currentUser!!.uid}"

        Glide.with(this@SettingActivity)
            .load(R.drawable.sweet_potato_design)
            .into(iv!!)

        btnLogout!!.setOnClickListener {
            signOut()
            Log.d(TAG, "고구마켓이 종료되었습니다.")
        }
        btnRevoke!!.setOnClickListener {
            revokeAccess()
            Log.d(TAG, "고구마켓이 종료되었습니다.")
        }

        btntoken!!.setOnClickListener {
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }

                val token = task.result

                val msg = getString(R.string.msg_token_fmt, token)
                Log.d(TAG, msg)
                Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
            })
        }
    }

    public override fun onStart() {
        // 어플을 실행할 때 마다 Logcat 시스템으로 알려줌
        super.onStart()
        Log.d(ContentValues.TAG, "SettingActivity가 실행되었습니다.")
        Log.d(ContentValues.TAG, "SettingActivity - onStart() called")
    }

    public override fun onResume() {
        super.onResume()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_navigation_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val menuId = item.itemId
        when (menuId) {
            R.id.menu_search -> Toast.makeText(this, "Search Clicked", Toast.LENGTH_SHORT).show()
            R.id.menu_category -> Toast.makeText(this, "Category Clicked", Toast.LENGTH_SHORT)
                .show()
            R.id.menu_notification -> Toast.makeText(
                this,
                "Notifiation Clicked",
                Toast.LENGTH_SHORT
            )
                .show()
            else -> {}
        }
        return super.onOptionsItemSelected(item)
    }

    private fun signOut() {
        val account = GoogleSignIn.getLastSignedInAccount(this)
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.alert_popup, null)
        val textView: TextView = view.findViewById(R.id.textView)
        textView.text = "로그아웃을 하시겠습니까?"
        val alertdialog = AlertDialog.Builder(this)
            .setTitle("로그아웃 페이지")
            .setPositiveButton("예") { dialog, which ->

                TwitterCore.getInstance().sessionManager.clearActiveSession()
                FirebaseAuth.getInstance().signOut()
                LoginManager.getInstance().logOut()
                Session.getCurrentSession().close()
                if (account !== null) {
                    googleSignInClient.signOut().addOnCompleteListener(this) {
                    }
                }
                Toast.makeText(applicationContext, "로그아웃이 완료되었습니다.", Toast.LENGTH_SHORT)
                    .show()
                Log.d(TAG, "로그아웃이 완료되었습니다.")
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
                Log.d(TAG, "로그인 화면으로 이동합니다.")
                Toast.makeText(applicationContext, "로그인 화면으로 이동합니다.", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("아니오", null)
            .create()

        alertdialog.setView(view)
        alertdialog.show()
    }

    private fun signOut2() {
        val account = GoogleSignIn.getLastSignedInAccount(this)

        TwitterCore.getInstance().sessionManager.clearActiveSession()
        FirebaseAuth.getInstance().signOut()
        LoginManager.getInstance().logOut()
        Session.getCurrentSession().close()

        if (account !== null) {
            googleSignInClient.signOut().addOnCompleteListener(this) {
            }
        }
    }

    private fun revokeAccess() {
        val account = GoogleSignIn.getLastSignedInAccount(this)
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.alert_popup, null)
        val textView: TextView = view.findViewById(R.id.textView)
        textView.text = "회원탈퇴를 하시겠습니까?"
        val alertdialog = AlertDialog.Builder(this)
            .setTitle("회원탈퇴 페이지")
            .setPositiveButton("Yes") { dialog, which ->

                TwitterCore.getInstance().sessionManager.clearActiveSession()
                if (auth!!.currentUser != null) {
                    auth!!.currentUser!!.delete()
                }

                fbFireStore?.collection("users")?.document(auth?.uid.toString())?.delete()
                databaseReference.child("Users").child(auth?.currentUser!!.uid).removeValue()
                FirebaseAuth.getInstance().signOut()
                LoginManager.getInstance().logOut()
                Session.getCurrentSession().close()

                if (account !== null) {
                    googleSignInClient.revokeAccess().addOnCompleteListener(this) {
                    }
                }
                Toast.makeText(applicationContext, "회원탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT)
                    .show()
                Log.d(TAG, "회원탈퇴가 완료되었습니다.")
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
                Log.d(TAG, "로그인 화면으로 이동합니다.")
                Toast.makeText(applicationContext, "로그인 화면으로 이동합니다.", Toast.LENGTH_SHORT).show()
            }

            .setNegativeButton("No", null)
            .create()

        alertdialog.setView(view)
        alertdialog.show()
    }

    override fun onBackPressed() {
        second_time = System.currentTimeMillis()
        if (second_time - first_time < 2000) {
            super.onBackPressed()
            signOut2()
            Log.d(TAG, "고구마켓이 종료되었습니다.")
            finishAffinity()
        } else {
            Toast.makeText(this@SettingActivity, "뒤로가기를 한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT)
                .show()
        }
        first_time = System.currentTimeMillis()
    }
}