package com.example.app.fakecarrotmarket

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.example.app.fakecarrotmarket.Adapter.DBListViewAdapter
import com.example.app.fakecarrotmarket.DataBase.ChatUser
import com.example.app.fakecarrotmarket.DataBase.GoguMarketDB
import com.example.app.fakecarrotmarket.databinding.FragmentHomeBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.ArrayList

var auth: FirebaseAuth? = null

val goguMarketDataBase: DatabaseReference by lazy {
    Firebase.database.reference.child(DBKey.DB_MAIN)
}

class HomeFragment : Fragment() {

    private var DBListView: ListView? = null

    var goguMarketDB = arrayListOf<GoguMarketDB>()
    private var temp2: String? = null
    private var mKeys: ArrayList<String> = ArrayList()
    private var binding: FragmentHomeBinding? = null
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

        val sharedPreference = this.requireActivity()
            .getSharedPreferences("temp record", AppCompatActivity.MODE_PRIVATE)
        val editor = sharedPreference.edit()
        val savedUID = sharedPreference.getString("id", "")
        val savedTemp = sharedPreference.getString("temp", "")
        val savedPreset = sharedPreference.getStringSet("preset", mutableSetOf())
        temp2 = savedTemp

        if (savedUID != "") {
            if (auth?.currentUser!!.uid != savedUID) {
                Log.d(TAG, "과거 고구마켓 ID, 무작위 계정 현황 => $savedPreset")
                val randomMath = Random()
                var num = randomMath.nextInt(9999) + 1
                while (num < 1000) {
                    num = randomMath.nextInt(9999) + 1
                }
                temp2 = num.toString()

                savedPreset!!.add("${auth?.currentUser!!.uid} : 고구마켓${temp2}")
                Log.d(TAG, "현재 고구마켓 ID, 무작위 계정 현황 => $savedPreset")
                editor.putString("id", auth?.currentUser!!.uid)
                editor.putString("temp", temp2)
                editor.putStringSet("preset", savedPreset)
                editor.apply()
                uploadAccount(auth?.currentUser!!.uid, "고구마켓" + temp2!!)
                Log.d(TAG, "고구마켓 DB에 계정 업로드가 성공적으로 수행되었습니다!")
            } else {
                Log.d(TAG, "현재 고구마켓 ID, 무작위 계정 현황 => $savedPreset")
            }
        } else {
            Log.d(TAG, "고구마켓 채팅방으로 들어가서 계정을 생성하세요.")
        }

        if (temp2 != "") {
            Log.d(TAG, "현재 접속중인 무작위 회원 : 고구마켓$temp2")
        }

    }

    override fun onStart() {
        // 어플을 실행할 때 마다 Logcat 시스템으로 알려줌
        super.onStart()
        Log.d(TAG, "HomeFragment가 실행되었습니다.")
        Log.d(TAG, "HomeFragment - onStart() called")
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(
            R.layout.fragment_home,
            container,
            false
        )
        DBListView = view.findViewById(R.id.DBListView) as ListView
        showGoguMarketDB()
        // Inflate the layout for this fragment
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fragmentHomeBinding = FragmentHomeBinding.bind(view)
        binding = fragmentHomeBinding

        fragmentHomeBinding.addFloatingButton.setOnClickListener {
            if (auth?.currentUser != null) {
                val intent = Intent(activity, AddArticleActivity::class.java)
                startActivity(intent)
            } else {
                Snackbar.make(view, "로그인 후 사용해주세요", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun uploadAccount(userId: String, userTemp: String) {
        val model = ChatUser(userId, userTemp)
        chatUserDB.child(auth?.currentUser!!.uid).setValue(model)
    }

    private fun uploadGoguMarketDB(
        productName: String?,
        productImgUrl: String?,
        userUid: String?,
        userName: String?,
        userAddress: String?,
        userTemper: Double?,
        productId: String?,
        category: String?,
        uploadTime: String?,
        content: String?,
        likeCount: Int?,
        price: Int?
    ) {
        val model = GoguMarketDB(
            productName,
            productImgUrl,
            userUid,
            userName,
            userAddress,
            userTemper,
            productId,
            category,
            uploadTime,
            content,
            likeCount,
            price
        )
        goguMarketDataBase.child(productName!!).setValue(model)
    }

    private fun showGoguMarketDB() {
        val adapter =
            DBListViewAdapter(requireActivity(), goguMarketDB)
        DBListView!!.adapter = adapter

        databaseReference.child("GoguMarketDB")
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                    Log.e("LOG", "dataSnapshot.getKey() : " + dataSnapshot.key)
                    val value = dataSnapshot.getValue(GoguMarketDB::class.java)
                    val key = dataSnapshot.key
                    value?.let { goguMarketDB.add(it) }
                    key?.let { mKeys.add(it) }
                    adapter.notifyDataSetChanged()
                }


                override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}
                override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
                override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
                override fun onCancelled(databaseError: DatabaseError) {}
            })
        goguMarketDB.clear() // 여러개 생기지 않게 초기화 작업 (GoguMarketDB 전체 Value값)
        mKeys.clear() // 여러개 생기지 않게 초기화 작업 (dataSnapshot Key값)
    } // Upload 했을 때 ProductName에 따라 가나다.. 순서로 정렬됨
}