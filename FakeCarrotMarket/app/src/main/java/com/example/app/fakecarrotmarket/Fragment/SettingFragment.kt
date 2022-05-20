package com.example.app.fakecarrotmarket

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

class SettingFragment : Fragment() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private var temp: String? = null
    private var inputId: TextView? = null
    private var inputName: TextView? = null
    private var iv: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

        val sharedPreference = this.requireActivity()
            .getSharedPreferences("temp record", AppCompatActivity.MODE_PRIVATE)
        val savedTemp = sharedPreference.getString("temp", "")
        temp = savedTemp.toString()
        if (temp != "") {
            Log.d(TAG, "현재 접속중인 무작위 회원 : 고구마켓$temp")
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view: View = inflater.inflate(
            R.layout.fragment_setting,
            container,
            false
        )

        iv = view.findViewById(R.id.iv2) as ImageView
        inputId = view.findViewById(R.id.input_id2) as TextView
        inputName = view.findViewById(R.id.input_name2) as TextView

        val tempName = "고구마켓$temp"
        inputName!!.text = "무작위 닉네임 : $tempName"
        inputId!!.text = "현재 접속중인 ID : ${auth?.currentUser!!.uid}"

        Glide.with(this@SettingFragment)
            .load(R.drawable.sweet_potato_design)
            .into(iv!!)

        // Inflate the layout for this fragment
        return view
    }

    override fun onStart() {
        // 어플을 실행할 때 마다 Logcat 시스템으로 알려줌
        super.onStart()
        Log.d(TAG, "SettingFragment가 실행되었습니다.")
        Log.d(TAG, "SettingFragment - onStart() called")
    }

    override fun onResume() {
        super.onResume()
    }
}