package com.example.app.fakecarrotmarket

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.example.app.fakecarrotmarket.DataBase.ChatUser
import com.example.app.fakecarrotmarket.databinding.FragmentHomeBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import java.util.*

var auth: FirebaseAuth? = null

class HomeFragment : Fragment() {

    private var temp2: String? = null
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
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
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
}