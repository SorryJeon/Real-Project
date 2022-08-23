package com.example.app.fakecarrotmarket

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.KeyEvent.KEYCODE_ENTER
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase

class ChatActivity2 : AppCompatActivity() {

    private var temp: String? = null
    private var CHAT_NAME: String? = null
    private var USER_NAME: String? = null
    private var chat_view: ListView? = null
    private var chat_edit: EditText? = null
    private var chat_send: Button? = null
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val databaseReference = firebaseDatabase.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat2)

        val actionBar: ActionBar? = supportActionBar
        actionBar?.title = "GoguMarket 채팅방"

        // 위젯 ID 참조
        chat_view = findViewById<View>(R.id.chat_view) as ListView
        chat_edit = findViewById<View>(R.id.chat_edit) as EditText
        chat_send = findViewById<View>(R.id.chat_sent) as Button

        // 로그인 화면에서 받아온 채팅방 이름, 유저 이름 저장
        val intent = intent
        CHAT_NAME = intent.getStringExtra("chatName")
        USER_NAME = intent.getStringExtra("userName")

        // 채팅 방 입장
        openChat(CHAT_NAME)

        // Activity 변경혹은 어플 종료시 temp값이 초기화 되므로, 초기화를 없애는 저장 기능 사용
        val sharedPreference = getSharedPreferences("temp record", MODE_PRIVATE)
        val savedTemp = sharedPreference.getString("temp", "")
        temp = savedTemp
        if (temp != "") {
            Log.d(TAG, "현재 접속중인 무작위 회원 : 고구마켓$temp")
        }

        // EditText 항목에 Enter Key 활성화 막기
        chat_edit!!.setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN
                && keyCode == KEYCODE_ENTER
            ) {
                // 키패드 내리기
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(chat_edit!!.windowToken, 0)
                // Toast Message
                if (chat_edit!!.text != null) {
                    showLogMessage(chat_edit!!.text.toString())
                }
                true
            }

            false
        }

        // 메시지 전송 버튼에 대한 클릭 리스너 지정
        chat_send!!.setOnClickListener(
            object : View.OnClickListener {
                override fun onClick(v: View?) {
                    if (chat_edit!!.text.toString() == "") return
                    val chat =
                        ChatDTO(USER_NAME, chat_edit!!.text.toString()) //ChatDTO를 이용하여 데이터를 묶는다.
                    databaseReference.child("chat").child(CHAT_NAME!!).push()
                        .setValue(chat) // 데이터 푸쉬
                    Log.d(TAG, "$USER_NAME : ${chat_edit!!.text}")
                    chat_edit!!.setText("") //입력창 초기화
                }
            })
    }

    public override fun onStart() {
        // 어플을 실행할 때 마다 Logcat 시스템으로 알려줌
        super.onStart()
        Log.d(TAG, "ChatActivity2가 실행되었습니다.")
        Log.d(TAG, "ChatActivity2 - onStart() called")
    }

    override fun onBackPressed() {
        Toast.makeText(this, "$CHAT_NAME 채팅방에서 퇴장하셨습니다.", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "$USER_NAME 님이 $CHAT_NAME 채팅방에서 퇴장하셨습니다.")
        super.onBackPressed()
    }

    private fun addMessage(dataSnapshot: DataSnapshot, adapter: ArrayAdapter<String>) {
        val chatDTO = dataSnapshot.getValue(ChatDTO::class.java)
        adapter.add(chatDTO!!.userName + "\n: " + chatDTO.message)
    }

    private fun removeMessage(dataSnapshot: DataSnapshot, adapter: ArrayAdapter<String>) {
        val chatDTO = dataSnapshot.getValue(ChatDTO::class.java)
        adapter.remove(chatDTO!!.userName + "\n: " + chatDTO.message)
    }

    private fun openChat(chatName: String?) {
        // 리스트 어댑터 생성 및 세팅
        val adapter =
            ArrayAdapter<String>(this, R.layout.chating_listview, R.id.text_chat)
        chat_view!!.adapter = adapter

        // 데이터 받아오기 및 어댑터 데이터 추가 및 삭제 등..리스너 관리
        databaseReference.child("chat").child(chatName!!)
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                    addMessage(dataSnapshot, adapter)
                    Log.e("LOG", "s:$s")
                }

                override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}
                override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                    removeMessage(dataSnapshot, adapter)
                }

                override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.chat_navigation_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val menuId = item.itemId
        when (menuId) {
            R.id.menu_goBack -> onBackPressed()
            else -> {}
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showLogMessage(msg: String?) {
        Log.d(TAG, msg.toString())
    }
}