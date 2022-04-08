package com.example.app.fakecarrotmarket

import android.content.ContentValues.TAG
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
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.kakao.auth.Session
import com.twitter.sdk.android.core.TwitterCore

import com.google.firebase.database.DatabaseError

import com.google.firebase.database.DataSnapshot

import com.google.firebase.database.ChildEventListener


class ChatActivity : AppCompatActivity() {

    private val mContext: Context = this@ChatActivity
    private val ACTIVITY_NUM = 1
    var first_time: Long = 0
    var second_time: Long = 0
    var auth: FirebaseAuth? = null
    private lateinit var googleSignInClient: GoogleSignInClient

    private var user_chat: EditText? = null
    private var user_edit: EditText? = null
    private var user_next: Button? = null
    private var user_delete: Button? = null
    private var chat_list: ListView? = null

    private var firebaseDatabase = FirebaseDatabase.getInstance()
    private var databaseReference = firebaseDatabase.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val actionBar: ActionBar? = supportActionBar
        actionBar?.hide()

        user_chat = findViewById(R.id.user_chat)
        user_edit = findViewById(R.id.user_edit)
        user_next = findViewById(R.id.user_next)
        user_delete = findViewById(R.id.user_delete)
        chat_list = findViewById(R.id.chat_list)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setupBottomNavigationView()

//        val database: FirebaseDatabase = FirebaseDatabase.getInstance()
//        val databaseReference: DatabaseReference = database.getReference("message")
//        databaseReference.setValue("Hello World!")

        user_next!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if (user_edit!!.text.toString() == "" || user_chat!!.text.toString() == "") return
                val intent = Intent(this@ChatActivity, ChatActivity2::class.java)
                intent.putExtra("chatName", user_chat!!.text.toString())
                intent.putExtra("userName", user_edit!!.text.toString())
                startActivity(intent)
            }
        })
        user_delete!!.setOnClickListener {
            onDialog()
        }

        showChatList()

        val intent = intent
        val currentAccount = intent.getStringExtra("currentAccount")

        chat_list!!.setOnItemClickListener { parent, view, position, id ->

            val element = parent.getItemAtPosition(position) as String
            val intent = Intent(this@ChatActivity, ChatActivity2::class.java)
            intent.putExtra("chatName", element)
            intent.putExtra("userName", currentAccount)
            Log.d(TAG, "${element} 채팅방으로 이동합니다.")
            startActivity(intent)

        }
    }

    public override fun onStart() {
        // 어플을 실행할 때 마다 Logcat 시스템으로 알려줌
        super.onStart()
        Log.d(TAG, "ChatActivity가 실행되었습니다.")
        Log.d(TAG, "ChatActivity - onStart() called")
    }

    private fun showChatList() {
        // 리스트 어댑터 생성 및 세팅
        val adapter =
            ArrayAdapter<String>(this, R.layout.custom_listview, R.id.text_title)
        chat_list!!.adapter = adapter

        // 데이터 받아오기 및 어댑터 데이터 추가 및 삭제 등..리스너 관리
        databaseReference.child("chat").addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                Log.e("LOG", "dataSnapshot.getKey() : " + dataSnapshot.key)
                adapter.add(dataSnapshot.key)
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun deleteChatList() {
        // 리스트 어댑터 생성 및 세팅
        val adapter =
            ArrayAdapter<String>(this, R.layout.custom_listview, R.id.text_title)
        chat_list!!.adapter = adapter

        // 데이터 받아오기 및 어댑터 데이터 추가 및 삭제 등..리스너 관리
        databaseReference.child("chat").removeEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {}

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                adapter.remove(dataSnapshot.key)
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onCancelled(databaseError: DatabaseError) {}
        })
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

    private fun onDialog() {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.alert_popup, null)
        val textView: TextView = view.findViewById(R.id.textView)
        textView.text = "채팅방 전체를 삭제하시겠습니까?"
        Log.d(TAG, "채팅방 삭제 Dialog 활성화.")
        val alertdialog = AlertDialog.Builder(this)
            .setTitle("채팅방 삭제여부 페이지")
            .setPositiveButton("예") { dialog, which ->
                deleteChatList()
                databaseReference.child("chat").removeValue()
                Toast.makeText(applicationContext, "채팅방이 모두 삭제되었습니다.", Toast.LENGTH_SHORT)
                    .show()
                Log.d(TAG, "채팅방이 모두 삭제되었습니다.")
                Log.d(TAG, "채팅방 삭제 Dialog 종료.")
            }
            .setNegativeButton("아니오") { dialog, which ->
                Toast.makeText(applicationContext, "채팅방 삭제가 취소되었습니다.", Toast.LENGTH_SHORT)
                    .show()
                Log.d(TAG, "채팅방 삭제를 취소하겠습니다.")
                Log.d(TAG, "채팅방 삭제 Dialog 종료.")
            }
            .create()

        alertdialog.setView(view)
        alertdialog.show()
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