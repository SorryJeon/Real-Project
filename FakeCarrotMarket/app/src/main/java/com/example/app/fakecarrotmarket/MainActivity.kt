package com.example.app.fakecarrotmarket

import android.content.Intent
import android.graphics.Bitmap
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


class MainActivity : AppCompatActivity() {
    var btnRevoke: Button? = null
    var btnLogout: Button? = null
    var btnexit: Button? = null
    var btnimage: Button? = null
    var btnupload: Button? = null
    var auth: FirebaseAuth? = null
    var iv: ImageView? = null
    var imgUri: Uri? = null
    var bitUri: Bitmap? = null
    var fbFirestore: FirebaseFirestore? = null
    var fbStorage: FirebaseStorage? = null
    var fbFireStore: FirebaseFirestore? = null

    private lateinit var googleSignInClient: GoogleSignInClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnLogout = findViewById<View>(R.id.btn_logout) as Button
        btnRevoke = findViewById<View>(R.id.btn_revoke) as Button
        btnexit = findViewById<View>(R.id.btn_exit) as Button
        btnimage = findViewById<View>(R.id.btn_image) as Button
        btnupload = findViewById<View>(R.id.btn_upload) as Button
        iv = findViewById<View>(R.id.iv) as ImageView
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
        btnexit!!.setOnClickListener {
            finishAffinity()
        }

        btnimage!!.setOnClickListener {
            clickSelect(this)
        }

        btnupload!!.setOnClickListener {
            clickUpload(this)
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
        LoginManager.getInstance().logOut()
        if (account !== null) {
            googleSignInClient.revokeAccess().addOnCompleteListener(this) {
            }
        }
    }

//    fun clickLoad(view: View?) {
//
//        //Firebase Storage에 저장되어 있는 이미지 파일 읽어오기
//
//        //1. Firebase Storeage관리 객체 얻어오기
//        val firebaseStorage = FirebaseStorage.getInstance()
//
//        //2. 최상위노드 참조 객체 얻어오기
//        val rootRef = firebaseStorage.reference
//
//        //읽어오길 원하는 파일의 참조객체 얻어오기
//        //예제에서는 자식노드 이름은 monkey.png
//        var imgRef = rootRef.child("monkey.png")
//        //하위 폴더가 있다면 폴더명까지 포함하여
//        imgRef = rootRef.child("photo/bazzi.png")
//        if (imgRef != null) {
//            //참조객체로 부터 이미지의 다운로드 URL을 얻어오기
//            imgRef.downloadUrl.addOnSuccessListener { uri -> //다운로드 URL이 파라미터로 전달되어 옴.
//                Glide.with(this@MainActivity).load(uri)
//                    .into(iv!!)
//            }
//        }
//    }

    fun clickSelect(view: MainActivity) {
        //사진을 선탣할 수 있는 Gallery앱 실행
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
            }
        }
    }


    fun clickUpload(view: MainActivity) {

        var timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imgFileName = "IMAGE_" + timeStamp + "_.png"
        var storageRef = fbStorage?.reference?.child("images")?.child(imgFileName)

        storageRef?.putFile(imgUri!!)?.addOnSuccessListener {
            Toast.makeText(view.baseContext, "Image Uploaded", Toast.LENGTH_SHORT).show()
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                var userInfo = ModelUsers()
                userInfo.imageUrl = uri.toString()

                fbFirestore?.collection("users")?.document(auth?.uid.toString())
                    ?.update("imageUrl", userInfo.imageUrl.toString())
            }
        }
    }
}