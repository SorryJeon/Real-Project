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
import java.text.SimpleDateFormat
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
                Log.d(TAG, "?????? ???????????? ID, ????????? ?????? ?????? => $savedPreset")
                val randomMath = Random()
                var num = randomMath.nextInt(9999) + 1
                while (num < 1000) {
                    num = randomMath.nextInt(9999) + 1
                }
                temp2 = num.toString()

                savedPreset!!.add("${auth?.currentUser!!.uid} : ????????????${temp2}")
                Log.d(TAG, "?????? ???????????? ID, ????????? ?????? ?????? => $savedPreset")
                editor.putString("id", auth?.currentUser!!.uid)
                editor.putString("temp", temp2)
                editor.putStringSet("preset", savedPreset)
                editor.apply()
                uploadAccount(auth?.currentUser!!.uid, "????????????" + temp2!!)
                Log.d(TAG, "???????????? DB??? ?????? ???????????? ??????????????? ?????????????????????!")
            } else {
                Log.d(TAG, "?????? ???????????? ID, ????????? ?????? ?????? => $savedPreset")
            }
        } else {
            Log.d(TAG, "???????????? ??????????????? ???????????? ????????? ???????????????.")
        }

        if (temp2 != "") {
            Log.d(TAG, "?????? ???????????? ????????? ?????? : ????????????$temp2")
        }

    }

    override fun onStart() {
        // ????????? ????????? ??? ?????? Logcat ??????????????? ?????????
        super.onStart()
        Log.d(TAG, "HomeFragment??? ?????????????????????.")
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
                Snackbar.make(view, "????????? ??? ??????????????????", Snackbar.LENGTH_LONG).show()
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
        goguMarketDB.clear() // ????????? ????????? ?????? ????????? ?????? (GoguMarketDB ?????? Value???)
        mKeys.clear() // ????????? ????????? ?????? ????????? ?????? (dataSnapshot Key???)
    } // Upload ?????? ??? ProductName??? ?????? ?????????.. ????????? ?????????
}