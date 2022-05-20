package com.example.app.fakecarrotmarket

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.ListFragment
import com.example.app.fakecarrotmarket.DataBase.ChatUser
import com.example.app.fakecarrotmarket.databinding.FragmentChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*

var currentAccount: String? = null
var marketAccount: String? = null

private var temp: String? = null

private var firebaseDatabase = FirebaseDatabase.getInstance()
private var databaseReference = firebaseDatabase.reference

val chatUserDB: DatabaseReference by lazy {
    Firebase.database.reference.child(DBKey.DB_USERS)
}

class ChatFragment : ListFragment() {

    private var binding: FragmentChatBinding? = null
    private var chatList: ListView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        val extra = arguments
        if (extra != null) {
            currentAccount = extra.getString("currentAccount")
            marketAccount = currentAccount
        }

        val sharedPreference = this.requireActivity()
            .getSharedPreferences("temp record", AppCompatActivity.MODE_PRIVATE)
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
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(
            R.layout.fragment_chat,
            container,
            false
        )

        chatList = view.findViewById(android.R.id.list) as ListView
        showChatList()

        return view // Inflate the layout for this fragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fragmentChatBinding = FragmentChatBinding.bind(view)
        binding = fragmentChatBinding

        binding!!.list.setOnItemClickListener { parent, view, position, id ->
            val element = parent!!.getItemAtPosition(position) as String
            val intent = Intent(activity, ChatActivity2::class.java)
            intent.putExtra("chatName", element)
            intent.putExtra("userName", "고구마켓$temp")
            Log.d(TAG, "고구마켓$temp 유저가 $element 채팅방으로 이동합니다.")
            Toast.makeText(requireContext(), "$element 채팅방으로 이동합니다.", Toast.LENGTH_SHORT)
                .show()
            startActivity(intent)
        }
    }

    private fun uploadAccount(userId: String, userTemp: String) {
        val model = ChatUser(userId, userTemp)
        chatUserDB.child(marketAccount!!).setValue(model)
    }

    private fun showChatList() {
        val adapter =
            ArrayAdapter<String>(
                requireActivity(),
                R.layout.custom_listview,
                R.id.text_title
            )
        chatList!!.adapter = adapter
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
}