package com.example.app.fakecarrotmarket

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.facebook.FacebookSdk
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.messaging.FirebaseMessaging
import com.kakao.auth.Session
import com.twitter.sdk.android.core.TwitterCore
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    val TAG: String = "안녕"
    var first_time: Long = 0
    var second_time: Long = 0
    var btnRevoke: Button? = null
    var btnLogout: Button? = null
    var btnimage: Button? = null
    var btnupload: Button? = null
    var btndb: Button? = null
    var btntoken: Button? = null
    var auth: FirebaseAuth? = null
    var iv: ImageView? = null
    var tvafter: TextView? = null
    var imgUri: Uri? = null
    var fbStorage: FirebaseStorage? = null
    var fbFireStore: FirebaseFirestore? = null
    var nickname: TextView? = null

    private lateinit var googleSignInClient: GoogleSignInClient

    @SuppressLint("StringFormatInvalid")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var actionBar : ActionBar? = supportActionBar
        actionBar?.hide()

        FacebookSdk.sdkInitialize(getApplicationContext())
        btnLogout = findViewById<View>(R.id.btn_logout) as Button
        btnRevoke = findViewById<View>(R.id.btn_revoke) as Button
        btnimage = findViewById<View>(R.id.btn_image) as Button
        btnupload = findViewById<View>(R.id.btn_upload) as Button
        btndb = findViewById<View>(R.id.btn_db) as Button
        btntoken = findViewById<View>(R.id.btn_token) as Button
        iv = findViewById<View>(R.id.iv) as ImageView
        tvafter = findViewById<View>(R.id.tv_after) as TextView
        nickname = findViewById<View>(R.id.nickname) as TextView
        auth = FirebaseAuth.getInstance()
        fbStorage = FirebaseStorage.getInstance()
        fbFireStore = FirebaseFirestore.getInstance()

        btnLogout!!.setOnClickListener {
            signOut()
        }
        btnRevoke!!.setOnClickListener {
            revokeAccess()
        }

        btndb!!.setOnClickListener {
            clickLoad()
        }

        btnimage!!.setOnClickListener {
            clickSelect()
        }

        btnupload!!.setOnClickListener {
            clickUpload()
        }
        btntoken!!.setOnClickListener {
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }

                val token = task.result

                val msg = getString(R.string.msg_token_fmt, token)
                Log.d(TAG, msg)
                Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
            })
        }

        Glide.with(this@MainActivity)
            .load(R.drawable.background_image_size)
            .into(iv!!)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        if (auth!!.currentUser != null) {

            val userInfo = ModelUsers()

            userInfo.uid = auth?.uid
            userInfo.userId = auth?.currentUser?.email
            fbFireStore?.collection("users")?.document(auth?.uid.toString())?.set(userInfo)

        }

        bottomNavigationView.setOnNavigationItemSelectedListener(this)

        supportFragmentManager.beginTransaction().add(R.id.linearLayout, HomeFragment()).commit()
    }


    private fun signOut() {
        val account = GoogleSignIn.getLastSignedInAccount(this)
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.alert_popup, null)
        val textView: TextView = view.findViewById(R.id.textView)
        textView.text = "로그아웃을 하시겠습니까?"
        val alertdialog = AlertDialog.Builder(this)
            .setTitle("로그아웃 페이지")
            .setPositiveButton("Yes") { dialog, which ->

                TwitterCore.getInstance().sessionManager.clearActiveSession()
                FirebaseAuth.getInstance().signOut()
                LoginManager.getInstance().logOut()
                Session.getCurrentSession().close()
                if (account !== null) {
                    googleSignInClient.signOut().addOnCompleteListener(this) {
                    }
                }
                Toast.makeText(applicationContext, "로그아웃이 완료되었습니다.", Toast.LENGTH_SHORT)
                    .show()
                finishAffinity()
            }
            .setNegativeButton("No", null)
            .create()

        alertdialog.setView(view)
        alertdialog.show()

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

    private fun revokeAccess() {
        val account = GoogleSignIn.getLastSignedInAccount(this)
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.alert_popup, null)
        val textView: TextView = view.findViewById(R.id.textView)
        textView.text = "회원탈퇴를 하시겠습니까?"
        val alertdialog = AlertDialog.Builder(this)
            .setTitle("회원탈퇴 페이지")
            .setPositiveButton("Yes") { dialog, which ->

                TwitterCore.getInstance().sessionManager.clearActiveSession()
                if (auth!!.currentUser != null) {
                    auth!!.currentUser!!.delete()
                }

                fbFireStore?.collection("users")?.document(auth?.uid.toString())?.delete()
                FirebaseAuth.getInstance().signOut()
                LoginManager.getInstance().logOut()
                Session.getCurrentSession().close()

                if (account !== null) {
                    googleSignInClient.revokeAccess().addOnCompleteListener(this) {
                    }
                }
                Toast.makeText(applicationContext, "회원탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT)
                    .show()
                finishAffinity()
            }

            .setNegativeButton("No", null)
            .create()

        alertdialog.setView(view)
        alertdialog.show()

    }

    private fun clickLoad() {
        // Firebase Storage에 저장되어 있는 이미지 파일 읽어오기

        // Firebase Storeage 관리 객체 얻어오기
        val firebaseStorage = FirebaseStorage.getInstance()

        // 최상위노드 참조 객체 얻어오기
        val rootRef = firebaseStorage.reference

        // 하위 폴더가 있다면 폴더명까지 포함하여 읽어오길 원하는 파일의 참조객체 얻어오기
        val imgRef = rootRef.child("images/대단하다 발암의나라!.PNG")

        // 오프라인 상태에서도 접근을 허용하기 위해 로컬파일 상수 설정
        val localfile = File.createTempFile("images", "png")

        // 참조객체로 부터 이미지의 다운로드 URL을 얻어오기
        imgRef.downloadUrl.addOnSuccessListener { uri -> //다운로드 URL이 파라미터로 전달되어 옴.
            Glide.with(this@MainActivity)
                .load(uri)
                .into(iv!!)
            Toast.makeText(
                baseContext, "파일 DB에서 가져오기 성공!",
                Toast.LENGTH_SHORT
            ).show()
            tvafter!!.text = "파일 DB에서 가져오기 성공!"
        }

        imgRef.getFile(localfile).addOnSuccessListener {
            imgUri = localfile.toUri()
        }
    }

    private fun clickSelect() {
        //사진을 선택할 수 있는 Gallery앱 실행
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 10)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            10 -> if (resultCode == RESULT_OK) {
                //선택한 이미지의 경로 얻어오기
                imgUri = data?.data
                iv?.setImageURI(imgUri)
                Toast.makeText(
                    baseContext, "파일 로컬에서 가져오기 성공!",
                    Toast.LENGTH_SHORT
                ).show()
                tvafter!!.text = "파일 로컬에서 가져오기 성공!"
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when(item.itemId) {
            R.id.page_home -> {
                supportFragmentManager.beginTransaction().replace(R.id.linearLayout , HomeFragment()).commitAllowingStateLoss()
                return true
            }
            R.id.page_chat -> {
                supportFragmentManager.beginTransaction().replace(R.id.linearLayout, ChatFragment()).commitAllowingStateLoss()
                return true
            }
            R.id.page_account -> {
                supportFragmentManager.beginTransaction().replace(R.id.linearLayout, AccountFragment()).commitAllowingStateLoss()
                return true
            }
        }

        return false
    }

    private fun clickUpload() {

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imgFileName = "IMAGE_" + timeStamp + "_.png"
        val storageRef = fbStorage?.reference?.child("images")?.child(imgFileName)

        if (imgUri != null) {
            storageRef?.putFile(imgUri!!)?.addOnSuccessListener {
                Toast.makeText(baseContext, "이미지 업로드 성공!", Toast.LENGTH_SHORT).show()
                tvafter!!.text = "이미지 업로드 성공!"
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    val userInfo = ModelUsers()
                    userInfo.imageUrl = uri.toString()

                    fbFireStore?.collection("users")?.document(auth?.uid.toString())
                        ?.update("imageUrl", userInfo.imageUrl.toString())
                }
            }
        } else {
            Toast.makeText(
                baseContext, "이미지 업로드 실패!",
                Toast.LENGTH_SHORT
            ).show()
            tvafter!!.text = "이미지 업로드 실패!"
        }
    }

    override fun onBackPressed() {
        second_time = System.currentTimeMillis()
        if (second_time - first_time < 2000) {
            super.onBackPressed()
            signOut2()
            finishAffinity()
        } else {
            Toast.makeText(this@MainActivity, "뒤로가기를 한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT)
                .show()
        }
        first_time = System.currentTimeMillis()
    }
}