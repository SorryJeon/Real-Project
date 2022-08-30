package com.example.app.fakecarrotmarket

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.app.fakecarrotmarket.Adapter.SettingListAdapter
import com.example.app.fakecarrotmarket.DataBase.SettingListView
import com.example.app.fakecarrotmarket.databinding.FragmentSettingBinding
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.kakao.auth.Session
import com.twitter.sdk.android.core.TwitterCore

class SettingFragment : Fragment() {

    private lateinit var googleSignInClient: GoogleSignInClient
    var settingListView = arrayListOf<SettingListView>()
    var fbFireStore: FirebaseFirestore? = null
    private var binding: FragmentSettingBinding? = null
    private var temp: String? = null
    private var inputId: TextView? = null
    private var inputName: TextView? = null
    private var iv: ImageView? = null
    private var settingMenu: RecyclerView? = null

    private var firebaseDatabase = FirebaseDatabase.getInstance()
    private var databaseReference = firebaseDatabase.reference

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

    @SuppressLint("SetTextI18n")
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
        settingMenu = view.findViewById(R.id.settingMenu) as RecyclerView

        settingListView = arrayListOf(
            SettingListView("로그아웃"), SettingListView("회원탈퇴"), SettingListView("회원정보수정")
        )

        val settingAdapter = SettingListAdapter(requireActivity(), settingListView)

        settingAdapter.setMyItemClickListener(object : SettingListAdapter.MyItemClickListener {
            override fun onItemClick(position: Int) {
                when (position) {
                    0 -> {
                        signOut()
                    }
                    1 -> {
                        revokeAccess()
                    }
                    2 -> {
                        tokenSearch()
                    }
                    else -> Log.d(TAG, "아이템이 클릭되었습니다.")
                }
            }
        })

        settingMenu!!.adapter = settingAdapter

        val layout = LinearLayoutManager(requireActivity())
        settingMenu!!.layoutManager = layout
        settingMenu!!.setHasFixedSize(true)

        val tempName = "고구마켓$temp"
        inputName!!.text = "무작위 닉네임 : $tempName"
        inputId!!.text = "현재 접속중인 ID : ${auth?.currentUser!!.uid}"

        Glide.with(this@SettingFragment)
            .load(R.drawable.sweet_potato_design)
            .into(iv!!)

        // Inflate the layout for this fragment
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentSettingBinding = FragmentSettingBinding.bind(view)
        binding = fragmentSettingBinding

        binding!!.btnLogout2.setOnClickListener {
            signOut()
            Log.d(TAG, "고구마켓이 종료되었습니다.")
        }

        binding!!.btnRevoke2.setOnClickListener {
            revokeAccess()
            Log.d(TAG, "고구마켓이 종료되었습니다.")
        }

        binding!!.btnToken2.setOnClickListener {
            tokenSearch()
        }
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

    private fun signOut() {
        val account = GoogleSignIn.getLastSignedInAccount(requireContext())
        val inflater =
            requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.alert_popup, null)
        val textView: TextView = view.findViewById(R.id.textView)
        textView.text = "로그아웃을 하시겠습니까?"
        val alertdialog = AlertDialog.Builder(requireContext())
            .setTitle("로그아웃 페이지")
            .setPositiveButton("예") { dialog, which ->

                TwitterCore.getInstance().sessionManager.clearActiveSession()
                FirebaseAuth.getInstance().signOut()
                LoginManager.getInstance().logOut()
                Session.getCurrentSession().close()
                if (account !== null) {
                    googleSignInClient.signOut().addOnCompleteListener(requireActivity()) {
                    }
                }
                Toast.makeText(requireContext(), "로그아웃이 완료되었습니다.", Toast.LENGTH_SHORT)
                    .show()
                Log.d(TAG, "로그아웃이 완료되었습니다.")
                val intent = Intent(activity, LoginActivity::class.java)
                startActivity(intent)
                activity?.supportFragmentManager
                    ?.beginTransaction()
                    ?.remove(this)
                    ?.commit()
                Log.d(TAG, "로그인 화면으로 이동합니다.")
            }
            .setNegativeButton("아니오", null)
            .create()

        alertdialog.setView(view)
        alertdialog.show()
    }

    private fun revokeAccess() {
        val account = GoogleSignIn.getLastSignedInAccount(requireContext())
        val inflater =
            requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.alert_popup, null)
        val textView: TextView = view.findViewById(R.id.textView)
        textView.text = "회원탈퇴를 하시겠습니까?"
        val alertdialog = AlertDialog.Builder(requireContext())
            .setTitle("회원탈퇴 페이지")
            .setPositiveButton("Yes") { dialog, which ->

                TwitterCore.getInstance().sessionManager.clearActiveSession()
                if (auth!!.currentUser != null) {
                    auth!!.currentUser!!.delete()
                }

                fbFireStore?.collection("users")?.document(auth?.uid.toString())?.delete()
                databaseReference.child("Users").child(auth?.currentUser!!.uid).removeValue()
                FirebaseAuth.getInstance().signOut()
                LoginManager.getInstance().logOut()
                Session.getCurrentSession().close()

                if (account !== null) {
                    googleSignInClient.revokeAccess().addOnCompleteListener(requireActivity()) {
                    }
                }
                Toast.makeText(requireContext(), "회원탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT)
                    .show()
                Log.d(TAG, "회원탈퇴가 완료되었습니다.")
                val intent = Intent(activity, LoginActivity::class.java)
                startActivity(intent)

                Log.d(TAG, "로그인 화면으로 이동합니다.")
            }

            .setNegativeButton("No", null)
            .create()

        alertdialog.setView(view)
        alertdialog.show()
    }

    private fun tokenSearch() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            val token = task.result

            val msg = getString(R.string.msg_token_fmt, token)
            Log.d(TAG, msg)
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        })
    }
}