package com.example.app.fakecarrotmarket

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
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.kakao.auth.Session
import com.twitter.sdk.android.core.TwitterCore

class SettingActivity : AppCompatActivity() {

    val TAG: String = "안녕"

    private val mContext: Context = this@SettingActivity
    private val ACTIVITY_NUM = 2
    private var temp: String? = null
    var first_time: Long = 0
    var second_time: Long = 0
    var iv: ImageView? = null
    var tvafter: TextView? = null
    var auth: FirebaseAuth? = null
    var btntoken: Button? = null
    var btnRevoke: Button? = null
    var btnLogout: Button? = null
    var fbStorage: FirebaseStorage? = null
    var fbFireStore: FirebaseFirestore? = null
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        btnLogout = findViewById<View>(R.id.btn_logout) as Button
        btnRevoke = findViewById<View>(R.id.btn_revoke) as Button
        btntoken = findViewById<View>(R.id.btn_token) as Button
        tvafter = findViewById<View>(R.id.tv_after) as TextView
        iv = findViewById<View>(R.id.iv) as ImageView
        auth = FirebaseAuth.getInstance()
        fbStorage = FirebaseStorage.getInstance()
        fbFireStore = FirebaseFirestore.getInstance()

        val actionBar: ActionBar? = supportActionBar
        actionBar?.hide()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setupBottomNavigationView()

        val sharedPreference = getSharedPreferences("temp record", MODE_PRIVATE)
        val savedTemp = sharedPreference.getString("temp", "")
        temp = savedTemp.toString()
        if (temp != "") {
            Log.d(ContentValues.TAG, "현재 접속중인 무작위 회원 : 고구마켓$temp")
        }

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

    private fun setupBottomNavigationView() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        enableNavigation(mContext, bottomNavigationView)
        val menu: Menu = bottomNavigationView.menu
        val menuItem: MenuItem = menu.getItem(ACTIVITY_NUM)
        menuItem.isChecked = true
    }

    private fun enableNavigation(context: Context, view: BottomNavigationView) {
        view.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.page_home -> {
                    val intent1 = Intent(context, MainActivity::class.java) // 0
                    context.startActivity(intent1)

                }
                R.id.page_chat -> {
                    val intent2 = Intent(context, ChatActivity::class.java) // 1
                    intent2.putExtra("currentAccount",auth?.currentUser!!.uid)
                    context.startActivity(intent2)

                }
                R.id.page_setting -> {
                    if (this@SettingActivity != this@SettingActivity) {
                        val intent3 = Intent(context, SettingActivity::class.java) // 2
                        context.startActivity(intent3)
                    }
                }
            }
            false
        }
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
                finishAffinity()
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
                finishAffinity()
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