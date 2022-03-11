package com.example.app.fakecarrotmarket

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
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
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.example.app.fakecarrotmarket.DBKey.Companion.DB_ARTICLES
import com.example.app.fakecarrotmarket.databinding.ActivityMainBinding
import com.facebook.FacebookSdk
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.kakao.auth.Session
import com.twitter.sdk.android.core.TwitterCore
import java.io.File

class MainActivity : AppCompatActivity() {

    private val mContext: Context = this@MainActivity
    private val ACTIVITY_NUM = 0

    var first_time: Long = 0
    var second_time: Long = 0
    var temp: String? = "상품 종류"
    var btnimage: Button? = null
    var btnupload: Button? = null
    var btndb: Button? = null
    var superbtn: Button? = null
    var auth: FirebaseAuth? = null
    var iv: ImageView? = null
    var titleEditText: EditText? = null
    var priceEditText: EditText? = null
    var titleType: Button? = null
    var tvafter: TextView? = null
    var productType: TextView? = null
    var imgUri: Uri? = null
    var imgUrl: String = ""
    var fbStorage: FirebaseStorage? = null
    var fbFireStore: FirebaseFirestore? = null
    var nickname: TextView? = null

    val articleDB: DatabaseReference by lazy {
        Firebase.database.reference.child(DB_ARTICLES)
    }

    private lateinit var googleSignInClient: GoogleSignInClient
    lateinit var binding: ActivityMainBinding

    @SuppressLint("StringFormatInvalid")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val actionBar: ActionBar? = supportActionBar
        actionBar?.hide()

        FacebookSdk.sdkInitialize(getApplicationContext())
        btnimage = findViewById<View>(R.id.btn_image) as Button
        btnupload = findViewById<View>(R.id.btn_upload) as Button
        btndb = findViewById<View>(R.id.btn_db) as Button
        superbtn = findViewById<View>(R.id.btn_superUpload) as Button
        iv = findViewById<View>(R.id.iv) as ImageView
        titleEditText = findViewById<View>(R.id.titleEditText) as EditText
        priceEditText = findViewById<View>(R.id.priceEditText) as EditText
        titleType = findViewById<View>(R.id.titleType) as Button
        tvafter = findViewById<View>(R.id.tv_after) as TextView
        productType = findViewById<View>(R.id.product_type) as TextView
        nickname = findViewById<View>(R.id.nickname) as TextView
        auth = FirebaseAuth.getInstance()
        fbStorage = FirebaseStorage.getInstance()
        fbFireStore = FirebaseFirestore.getInstance()

        binding.titleType.setOnClickListener {
            val items = arrayOf("과일", "채소", "야채", "육류", "과자", "기타")
            val builder = AlertDialog.Builder(this)
            builder.setTitle("상품 항목을 골라주세요")
            builder.setItems(items) { dialog, which ->
                Toast.makeText(baseContext, "${items[which]} 항목이 선택되었습니다.", Toast.LENGTH_SHORT)
                    .show()
                Log.d(TAG, "${items[which]} 항목이 선택되었습니다.")
                productType!!.text = items[which]
                if (items[which] != temp) {
                    Log.d(TAG, "상품 종류가 ${items[which]}로 변경되었습니다.")
                }
                temp = items[which]
            }
            builder.show()
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

        superbtn!!.setOnClickListener {
            val title = titleEditText!!.text.toString()
            val price = priceEditText!!.text.toString()
            val sellerId = auth?.currentUser?.uid.orEmpty()
            if (title != "" && price != "" && imgUrl != "") {
                uploadArticle(sellerId, title, price, imgUrl)
                Log.d(TAG, "${auth?.currentUser.toString()}님이 ${title}을 ${price}에 등록하셨습니다.")
                Log.d(TAG, "${auth?.currentUser.toString()}님이 ${imgUrl}을 업로드하였습니다.")
            } else {
                Toast.makeText(
                    baseContext, "물건 제목과 가격을 입력해주세요 (이미지 첨부 필수)",
                    Toast.LENGTH_SHORT
                ).show()
            }
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
        setupBottomNavigationView()
    }

    public override fun onStart() {
        // 어플을 실행할 때 마다 Logcat 시스템으로 알려줌
        super.onStart()
        Log.d(TAG, "MainActivity가 실행되었습니다.")
        Log.d(TAG, "MainActivity - onStart() called")
    }

    private fun setupBottomNavigationView() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        enableNavigation(mContext, bottomNavigationView)
        val menu: Menu = bottomNavigationView.menu
        val menuItem: MenuItem = menu.getItem(ACTIVITY_NUM)
        menuItem.isChecked = true
    }

    private fun enableNavigation(context: Context, view: BottomNavigationView) {
        view.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.page_home -> {
                    if (this@MainActivity != this@MainActivity) {
                        val intent1 = Intent(context, MainActivity::class.java) // 0
                        context.startActivity(intent1)
                    }
                }
                R.id.page_chat -> {
                    val intent2 = Intent(context, ChatActivity::class.java) // 1
                    context.startActivity(intent2)
                }
                R.id.page_setting -> {
                    val intent3 = Intent(context, SettingActivity::class.java) // 2
                    context.startActivity(intent3)
                }
            }
            false
        }
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

    private fun uploadArticle(sellerId: String, title: String, price: String, imageUrl: String) {
        val model = ArticleModel(sellerId, title, System.currentTimeMillis(), price, imageUrl)
        articleDB.push().setValue(model)
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
            }
        }
    }

    private fun clickUpload() {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imgFileName = "IMAGE_" + timeStamp + "_.png"
        val storageRef = fbStorage?.reference?.child("images")?.child(imgFileName)

        if (imgUri != null) {
            storageRef?.putFile(imgUri!!)?.addOnSuccessListener {
                Toast.makeText(baseContext, "이미지 업로드 성공!", Toast.LENGTH_SHORT).show()
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    val userInfo = ModelUsers()
                    userInfo.imageUrl = uri.toString()
                    imgUrl = uri.toString()

                    fbFireStore?.collection("users")?.document(auth?.uid.toString())
                        ?.update("imageUrl", userInfo.imageUrl.toString())
                }
            }
        } else {
            Toast.makeText(
                baseContext, "이미지 업로드 실패!",
                Toast.LENGTH_SHORT
            ).show()
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