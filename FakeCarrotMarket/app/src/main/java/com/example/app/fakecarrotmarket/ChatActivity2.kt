package com.example.app.fakecarrotmarket

import android.content.ContentValues
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ChildEventListener
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBar
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.chating_listview.*


class ChatActivity2 : AppCompatActivity() {

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

        val backButton: Button? = findViewById(R.id.goBack)
        val actionBar: ActionBar? = supportActionBar
        actionBar?.hide()

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

        // 메시지 전송 버튼에 대한 클릭 리스너 지정
        chat_send!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if (chat_edit!!.text.toString() == "") return
                val chat = ChatDTO(USER_NAME, chat_edit!!.text.toString()) //ChatDTO를 이용하여 데이터를 묶는다.
                databaseReference.child("chat").child(CHAT_NAME!!).push().setValue(chat) // 데이터 푸쉬
                chat_edit!!.setText("") //입력창 초기화
            }
        })

        backButton!!.setOnClickListener {
            onBackPressed()
        }
    }

    public override fun onStart() {
        // 어플을 실행할 때 마다 Logcat 시스템으로 알려줌
        super.onStart()
        Log.d(ContentValues.TAG, "ChatActivity2가 실행되었습니다.")
        Log.d(ContentValues.TAG, "ChatActivity2 - onStart() called")
    }

    private fun addMessage(dataSnapshot: DataSnapshot, adapter: ArrayAdapter<String>) {
        val chatDTO = dataSnapshot.getValue(ChatDTO::class.java)
        adapter.add(chatDTO!!.userName + "\n" + chatDTO.message)
    }

    private fun removeMessage(dataSnapshot: DataSnapshot, adapter: ArrayAdapter<String>) {
        val chatDTO = dataSnapshot.getValue(ChatDTO::class.java)
        adapter.remove(chatDTO!!.userName + "\n" + chatDTO.message)
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
}