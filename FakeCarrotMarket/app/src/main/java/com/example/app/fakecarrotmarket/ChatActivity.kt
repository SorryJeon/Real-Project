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
import com.example.app.fakecarrotmarket.DataBase.ChatUser
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.kakao.auth.Session
import com.twitter.sdk.android.core.TwitterCore
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*


class ChatActivity : AppCompatActivity() {

    var first_time: Long = 0
    var second_time: Long = 0
    var auth: FirebaseAuth? = null
    private lateinit var googleSignInClient: GoogleSignInClient

    private var user_chat: EditText? = null
    private var user_edit: EditText? = null
    private var user_next: Button? = null
    private var user_delete: Button? = null
    private var chat_list: ListView? = null
    private var temp: String? = null

    private var firebaseDatabase = FirebaseDatabase.getInstance()
    private var databaseReference = firebaseDatabase.reference
    private var marketAccount: String? = null

    val chatUserDB: DatabaseReference by lazy {
        Firebase.database.reference.child(DBKey.DB_USERS)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val actionBar: ActionBar? = supportActionBar
        actionBar?.title = "채팅목록"

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

        user_next!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if (user_edit!!.text.toString() == "" || user_chat!!.text.toString() == "") return
                val intent = Intent(this@ChatActivity, ChatActivity2::class.java)
                intent.putExtra("chatName", user_chat!!.text.toString())
                intent.putExtra("userName", user_edit!!.text.toString())
                Log.d(TAG, "${user_chat!!.text} 유저가 ${user_edit!!.text} 채팅방으로 이동합니다.")
                Toast.makeText(
                    applicationContext,
                    "${user_chat!!.text} 유저가 ${user_edit!!.text} 채팅방으로 이동합니다.",
                    Toast.LENGTH_SHORT
                )
                    .show()
                startActivity(intent)
            }
        })
        user_delete!!.setOnClickListener {
            onDialog()
        }

        val intent = intent
        val currentAccount = intent.getStringExtra("currentAccount")
        marketAccount = currentAccount

        val sharedPreference = getSharedPreferences("temp record", MODE_PRIVATE)
        val savedTemp = sharedPreference.getString("temp", "")
        temp = savedTemp

        if (temp != "") {
            Log.d(TAG, "현재 접속중인 무작위 회원 : 고구마켓${temp}")
        } //어플에서 이미 저장된 temp값이 있는 경우 불러오기

        else {
            val randomMath = Random()
            var num = randomMath.nextInt(9999) + 1
            while (num < 1000) {
                num = randomMath.nextInt(9999) + 1
            }
            temp = num.toString()

            val editor = sharedPreference.edit()
            val mutableSet = hashSetOf("$currentAccount : 고구마켓${temp}")

            editor.putString("id", currentAccount)
            editor.putString("temp", temp)
            editor.putStringSet("preset", mutableSet)
            editor.apply() // Activity가 바뀌어도 앱을 종료할 때 까지 프로그램이 변경되지 않도록 수정.
            Log.d(TAG, "현재 생성된 무작위 회원 : 고구마켓${temp}")
            uploadAccount(currentAccount!!, "고구마켓" + temp!!)
            Log.d(TAG, "고구마켓 DB에 계정 업로드가 성공적으로 수행되었습니다!")
        } // 어플에서 이미 저장된 temp값이 없을 경우 새로 생성하기

        chat_list!!.setOnItemClickListener { parent, view, position, id ->

            val element = parent.getItemAtPosition(position) as String
            val intent = Intent(this@ChatActivity, ChatActivity2::class.java)
            intent.putExtra("chatName", element)
            intent.putExtra("userName", "고구마켓$temp")
            Log.d(TAG, "고구마켓$temp 유저가 $element 채팅방으로 이동합니다.")
            Toast.makeText(applicationContext, "$element 채팅방으로 이동합니다.", Toast.LENGTH_SHORT)
                .show()
            startActivity(intent)

        }
    }

    public override fun onStart() {
        // 어플을 실행할 때 마다 Logcat 시스템으로 알려줌
        super.onStart()
        Log.d(TAG, "ChatActivity가 실행되었습니다.")
        Log.d(TAG, "ChatActivity - onStart() called")
    }

    public override fun onResume() {
        super.onResume()

        Log.d(TAG, "고구마켓${temp}님이 접속중입니다.")
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        showChatList()
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

    private fun uploadAccount(userId: String, userTemp: String) {
        val model = ChatUser(userId, userTemp)
        chatUserDB.child(marketAccount!!).setValue(model)
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