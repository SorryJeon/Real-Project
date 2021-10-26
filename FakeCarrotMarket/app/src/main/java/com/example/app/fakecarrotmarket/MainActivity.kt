package com.example.app.fakecarrotmarket

import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import com.bumptech.glide.Glide
import com.facebook.AccessToken
import com.facebook.FacebookSdk
import com.facebook.GraphRequest
import com.facebook.HttpMethod
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    var first_time: Long = 0
    var second_time: Long = 0
    var btnRevoke: Button? = null
    var btnLogout: Button? = null
    var btnimage: Button? = null
    var btnupload: Button? = null
    var btndb: Button? = null
    var auth: FirebaseAuth? = null
    var iv: ImageView? = null
    var tvafter: TextView? = null
    var imgUri: Uri? = null
    var fbFirestore: FirebaseFirestore? = null
    var fbStorage: FirebaseStorage? = null
    var fbFireStore: FirebaseFirestore? = null

    private lateinit var googleSignInClient: GoogleSignInClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FacebookSdk.sdkInitialize(getApplicationContext())
        btnLogout = findViewById<View>(R.id.btn_logout) as Button
        btnRevoke = findViewById<View>(R.id.btn_revoke) as Button
        btnimage = findViewById<View>(R.id.btn_image) as Button
        btnupload = findViewById<View>(R.id.btn_upload) as Button
        btndb = findViewById<View>(R.id.btn_db) as Button
        iv = findViewById<View>(R.id.iv) as ImageView
        tvafter = findViewById<View>(R.id.tv_after) as TextView
        auth = FirebaseAuth.getInstance()
        fbStorage = FirebaseStorage.getInstance()
        fbFirestore = FirebaseFirestore.getInstance()

        btnLogout!!.setOnClickListener {
            signOut()
            finishAffinity()
        }
        btnRevoke!!.setOnClickListener {
            revokeAccess()
            finishAffinity()
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

        if (auth!!.currentUser != null) {

            var userInfo = ModelUsers()

            userInfo.uid = auth?.uid
            userInfo.userId = auth?.currentUser?.email
            fbFireStore?.collection("users")?.document(auth?.uid.toString())?.set(userInfo)
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

    }

    private fun signOut() {
        val account = GoogleSignIn.getLastSignedInAccount(this)
        FirebaseAuth.getInstance().signOut()
        LoginManager.getInstance().logOut()
        if (account !== null) {
            googleSignInClient.signOut().addOnCompleteListener(this) {
            }
        }
    }

    private fun revokeAccess() {
        val account = GoogleSignIn.getLastSignedInAccount(this)
        auth!!.currentUser!!.delete()
        FirebaseAuth.getInstance().signOut()
        if (AccessToken.getCurrentAccessToken() != null) {
            GraphRequest(
                AccessToken.getCurrentAccessToken(), "/me/permissions/", null, HttpMethod.DELETE,
                GraphRequest.Callback {
                    AccessToken.setCurrentAccessToken(null)
                    LoginManager.getInstance().logOut()
                }
            ).executeAsync()
        }
        if (account !== null) {
            googleSignInClient.revokeAccess().addOnCompleteListener(this) {
            }
        }
    }

    private fun clickLoad() {
        //Firebase Storage에 저장되어 있는 이미지 파일 읽어오기

        //1. Firebase Storeage관리 객체 얻어오기
        val firebaseStorage = FirebaseStorage.getInstance()

        //2. 최상위노드 참조 객체 얻어오기
        val rootRef = firebaseStorage.reference

        //3. 하위 폴더가 있다면 폴더명까지 포함하여 읽어오길 원하는 파일의 참조객체 얻어오기
        var imgRef = rootRef.child("images/대단하다 발암의나라!.PNG")

        //4. 참조객체로 부터 이미지의 다운로드 URL을 얻어오기
        imgRef.downloadUrl.addOnSuccessListener { imgUri -> //다운로드 URL이 파라미터로 전달되어 옴.
            Glide.with(this@MainActivity)
                .load(imgUri)
                .into(iv!!)
            Toast.makeText(
                baseContext, "파일 DB에서 가져오기 성공!",
                Toast.LENGTH_SHORT
            ).show()
            tvafter!!.text = "파일 DB에서 가져오기 성공!"
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


    private fun clickUpload() {

        var timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imgFileName = "IMAGE_" + timeStamp + "_.png"
        var storageRef = fbStorage?.reference?.child("images")?.child(imgFileName)

        if (imgUri != null) {
            storageRef?.putFile(imgUri!!)?.addOnSuccessListener {
                Toast.makeText(baseContext, "이미지 업로드 성공!", Toast.LENGTH_SHORT).show()
                tvafter!!.text = "이미지 업로드 성공!"
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    var userInfo = ModelUsers()
                    userInfo.imageUrl = uri.toString()

                    fbFirestore?.collection("users")?.document(auth?.uid.toString())
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
            signOut()
            finishAffinity()
        } else {
            Toast.makeText(this@MainActivity, "뒤로가기를 한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()
        }
    }
}