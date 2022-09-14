package com.example.app.fakecarrotmarket

import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.example.app.fakecarrotmarket.DataBase.GoguMarketDB
import com.example.app.fakecarrotmarket.databinding.ActivityAddArticleBinding
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.kakao.auth.Session
import com.twitter.sdk.android.core.TwitterCore
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AddArticleActivity : AppCompatActivity() {

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
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    var fbStorage: FirebaseStorage? = null
    var fbFireStore: FirebaseFirestore? = null

    val articleDB: DatabaseReference by lazy {
        Firebase.database.reference.child(DBKey.DB_ARTICLES)
    }

    private lateinit var googleSignInClient: GoogleSignInClient
    lateinit var binding: ActivityAddArticleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddArticleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        btnimage = findViewById<View>(R.id.btn_image2) as Button
        btnupload = findViewById<View>(R.id.btn_upload2) as Button
        btndb = findViewById<View>(R.id.btn_db2) as Button
        superbtn = findViewById<View>(R.id.btn_superUpload2) as Button
        iv = findViewById<View>(R.id.iv2) as ImageView
        titleEditText = findViewById<View>(R.id.titleEditText2) as EditText
        priceEditText = findViewById<View>(R.id.priceEditText2) as EditText
        titleType = findViewById<View>(R.id.titleType2) as Button
        tvafter = findViewById<View>(R.id.tv_after2) as TextView
        productType = findViewById<View>(R.id.product_type2) as TextView
        auth = FirebaseAuth.getInstance()
        fbStorage = FirebaseStorage.getInstance()
        fbFireStore = FirebaseFirestore.getInstance()

        val actionBar: ActionBar? = supportActionBar
        actionBar?.hide()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.titleType2.setOnClickListener {
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

        titleEditText!!.setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN
                && keyCode == KeyEvent.KEYCODE_ENTER
            ) {
                // 키패드 내리기
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(titleEditText!!.windowToken, 0)
                // Toast Message
                if (titleEditText!!.text != null) {
                    showLogMessage(titleEditText!!.text.toString())
                }
                true
            }

            false
        }

        priceEditText!!.setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN
                && keyCode == KeyEvent.KEYCODE_ENTER
            ) {
                // 키패드 내리기
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(priceEditText!!.windowToken, 0)
                // Toast Message
                if (priceEditText!!.text != null) {
                    showLogMessage(priceEditText!!.text.toString())
                }
                true
            }

            false
        }

        superbtn!!.setOnClickListener {

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

            val title = titleEditText!!.text.toString()
            val price = priceEditText!!.text.toString()
            val sellerId = auth?.currentUser?.uid.orEmpty()
            if (title != "" && price != "" && imgUrl != "") {
                uploadArticle(sellerId, title, price, imgUrl)
                uploadGoguMarketDB(
                    title,
                    imgUrl,
                    auth?.currentUser.toString(),
                    temp,
                    temp,
                    36.5,
                    temp,
                    productType!!.text.toString(),
                    timeStamp,
                    temp,
                    1,
                    price.toInt()
                )
                Log.d(TAG, "${auth?.currentUser.toString()}님이 ${title}을 ${price}에 등록하셨습니다.")
                Log.d(TAG, "${auth?.currentUser.toString()}님이 ${imgUrl}을 업로드하였습니다.")
                Toast.makeText(
                    baseContext, "${auth?.currentUser.toString()}님이 ${title}을 ${price}에 등록하셨습니다.",
                    Toast.LENGTH_SHORT
                ).show()

                Log.d(
                    TAG,
                    "${auth?.currentUser.toString()}님의 닉네임이 ${auth?.currentUser!!.uid}로 설정되었습니다."
                )
                Toast.makeText(
                    baseContext,
                    "${auth?.currentUser.toString()}님의 닉네임이 ${auth?.currentUser!!.uid}로 설정되었습니다.",
                    Toast.LENGTH_SHORT
                ).show()

                val intent = Intent(this@AddArticleActivity, ChatActivity2::class.java)
                intent.putExtra("chatName", title)
                intent.putExtra("userName", auth?.currentUser!!.uid)
                startActivity(intent)

            } else {
                Toast.makeText(
                    baseContext, "물건 제목과 가격을 입력해주세요 (이미지 첨부 필수)",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        Glide.with(this@AddArticleActivity)
            .load(R.drawable.sweet_potato_design2)
            .into(iv!!)

        if (auth?.currentUser != null) {

            val userInfo = ModelUsers()

            userInfo.uid = auth?.uid
            userInfo.userId = auth?.currentUser?.email
            fbFireStore?.collection("users")?.document(auth?.uid.toString())?.set(userInfo)

        }
    }

    public override fun onStart() {
        // 어플을 실행할 때 마다 Logcat 시스템으로 알려줌
        super.onStart()
        Log.d(TAG, "MainActivity가 실행되었습니다.")
        Log.d(TAG, "MainActivity - onStart() called")
    }

    public override fun onResume() {
        super.onResume()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_navigation_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val menuId = item.itemId
        when (menuId) {
            R.id.menu_search -> Toast.makeText(this, "Search Clicked", Toast.LENGTH_SHORT).show()
            R.id.menu_category -> Toast.makeText(this, "Category Clicked", Toast.LENGTH_SHORT)
                .show()
            R.id.menu_notification -> Toast.makeText(
                this,
                "Notifiation Clicked",
                Toast.LENGTH_SHORT
            )
                .show()
            else -> {}
        }
        return super.onOptionsItemSelected(item)
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
            Glide.with(this@AddArticleActivity)
                .load(uri)
                .into(iv!!)
            Toast.makeText(
                baseContext, "파일 DB에서 가져오기 성공!",
                Toast.LENGTH_SHORT
            ).show()
        }

        imgRef.getFile(localfile).addOnSuccessListener {
            imgUri = localfile.toUri()
            Log.d(TAG, imgUri.toString())
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

    private fun showLogMessage(msg: String?) {
        Log.d(TAG, msg.toString())
    }
}