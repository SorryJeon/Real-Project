package com.example.app.fakecarrotmarket

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.kakao.auth.Session
import com.twitter.sdk.android.core.TwitterCore
import kotlinx.android.synthetic.main.activity_main.*

class ChatActivity : AppCompatActivity() {

    private val mContext: Context = this@ChatActivity
    private val ACTIVITY_NUM = 1
    var first_time: Long = 0
    var second_time: Long = 0
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val actionBar: ActionBar? = supportActionBar
        actionBar?.hide()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setupBottomNavigationView()

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
                    if (this@ChatActivity != this@ChatActivity) {
                        val intent2 = Intent(context, ChatActivity::class.java) // 1
                        context.startActivity(intent2)
                    }
                }
                R.id.page_setting -> {
                    val intent3 = Intent(context, SettingActivity::class.java) // 2
                    context.startActivity(intent3)
                }
            }
            false
        }
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

    override fun onBackPressed() {
        second_time = System.currentTimeMillis()
        if (second_time - first_time < 2000) {
            super.onBackPressed()
            signOut2()
            finishAffinity()
        } else {
            Toast.makeText(this@ChatActivity, "뒤로가기를 한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT)
                .show()
        }
        first_time = System.currentTimeMillis()
    }
}