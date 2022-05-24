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
import java.text.SimpleDateFormat
import java.util.*

var auth: FirebaseAuth? = null
val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())

class HomeFragment : Fragment() {

    private var DBListView: ListView? = null

    var goguMarketDB = arrayListOf(
        GoguMarketDB(
            "닭강정세트",
            "google_logo_design2",
            "qwerty123",
            "홍길동",
            "서울특별시 서초구",
            36.5,
            "chicken001",
            "치킨류",
            timeStamp,
            "닭강정 오리지날맛",
            1,
            8000
        ), GoguMarketDB(
            "거의 사용안한 계산기",
            "facebook_logo_design2",
            "hojunjeon2000",
            "전호준",
            "서울특별시 성동구",
            36.5,
            "junggo001",
            "가전제품류",
            timeStamp,
            "계산기 거의 사용안한거니 구매하실분 귓말 주세요",
            1,
            6000
        ), GoguMarketDB(
            "광화문역 설렁탕 포장",
            "github_logo_design2",
            "notfakeProducter23",
            "서영호",
            "서울특별시 종로구",
            36.5,
            "chicken001",
            "국물류",
            timeStamp,
            "닭강정 오리지날맛",
            1,
            8000
        ), GoguMarketDB(
            "푸짐한 샐러드",
            "kakao_logo_design2",
            "coinsuit002",
            "이현민",
            "서울특별시 강남구",
            36.5,
            "chicken001",
            "샐러드류",
            timeStamp,
            "샐러드 푸짐한거 팝니다",
            1,
            5000
        ),
        GoguMarketDB(
            "뼈해장국",
            "twitter_logo_design2",
            "젠지_Streamer_Cuvee",
            "큐베",
            "서울특별시 중랑구",
            36.5,
            "baedal001",
            "배달음식류",
            timeStamp,
            "뼈해장국 큰 그릇",
            1,
            10000
        )
    )

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
    ): View {
        val view: View = inflater.inflate(
            R.layout.fragment_home,
            container,
            false
        )
        DBListView = view.findViewById(R.id.DBListView) as ListView
        val goguMarketAdapter = DBListViewAdapter(requireContext(), goguMarketDB)
        DBListView!!.adapter = goguMarketAdapter
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
}