package com.example.app.fakecarrotmarket

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Base64
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.*
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.kakao.auth.AuthType
import com.kakao.auth.Session
import com.kakao.sdk.common.util.Utility
import com.nhn.android.naverlogin.OAuthLogin
import com.nhn.android.naverlogin.OAuthLoginHandler
import com.twitter.sdk.android.core.*
import com.twitter.sdk.android.core.identity.TwitterAuthClient
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {
    lateinit var mOAuthLoginModule: OAuthLogin
    lateinit var twitterAuthClient: TwitterAuthClient
    var loginstate = false
    var first_time: Long = 0
    var second_time: Long = 0
    var auth: FirebaseAuth? = null
    val GOOGLE_REQUEST_CODE = 99
    val TAG = "googleLogin"
    var email: String? = null
    var accessToken: String? = null
    var callbackManager: CallbackManager? = null

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var callback: SessionCallback
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val actionBar: ActionBar? = supportActionBar
        actionBar?.hide()

        val authConfig = TwitterAuthConfig(
            getString(R.string.twitter_consumer_key),
            getString(R.string.twitter_consumer_secret)
        )

        val twitterConfig = TwitterConfig.Builder(this)
            .twitterAuthConfig(authConfig)
            .build()

        Twitter.initialize(twitterConfig)

        twitterAuthClient = TwitterAuthClient()

        callback = SessionCallback(this)

        val googleSignInBtn = findViewById<ImageButton>(R.id.googleSignInBtn)
        val facebookSignInBtn = findViewById<ImageButton>(R.id.facebookSignInBtn)
        val kakaoSignInBtn = findViewById<ImageButton>(R.id.kakaoSignInBtn)
        val twitterSignInBtn = findViewById<ImageButton>(R.id.twitterSignInBtn)
        val naverSignInBtn = findViewById<ImageButton>(R.id.naverSignInBtn)

        auth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        callbackManager = CallbackManager.Factory.create()

        googleSignInBtn.setOnClickListener {
            signIn()
        }

        facebookSignInBtn.setOnClickListener {
            facebookLogin()
        }

        kakaoSignInBtn.setOnClickListener {
            kakaoLoginStart()
        }

        twitterSignInBtn.setOnClickListener {
            if (!loginstate) {
                twitterLogin()
            } else {
                loginstate = !loginstate
            }
        }

        naverSignInBtn.setOnClickListener {
            mOAuthLoginModule = OAuthLogin.getInstance()
            mOAuthLoginModule.init(
                this,
                "DwBxwh3RiYjsumNlQaEo" // 네이버에서 앱 등록후 받은 ID값
                , "vCN3fiVqPQ" // 네이버에서 앱 등록후 받은 SECRET값
                , "DwBxwh3RiYjsumNlQaEo" // // 네이버에서 앱 등록후 받은 ID값
            )
            mOAuthLoginModule.startOauthLoginActivity(this, mOAuthLoginHandler)
        }

        // 로그인 버튼
        btn_login.setOnClickListener {
            //editText로부터 입력된 값을 받아온다
            signIn(edit_id.text.toString(), edit_pw.text.toString())
        }


        // 회원가입 버튼
        btn_register.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        btn_passReset.setOnClickListener {
            sendPasswordReset()
        }
    }

    @SuppressLint("HandlerLeak")
    private val mOAuthLoginHandler: OAuthLoginHandler = object : OAuthLoginHandler() {
        override fun run(success: Boolean) {
            if (success) {
                accessToken = mOAuthLoginModule.getAccessToken(baseContext)
                val refreshToken: String = mOAuthLoginModule.getRefreshToken(baseContext)
                val expiresAt: Long = mOAuthLoginModule.getExpiresAt(baseContext)
                val tokenType: String = mOAuthLoginModule.getTokenType(baseContext)
                Log.d(TAG, accessToken + "액세스 토큰")
                Log.d(TAG, refreshToken + "새로고침 토큰")
                Log.d(TAG, expiresAt.toString() + "만료 날짜")
                Log.d(TAG, tokenType + "토큰 종류")
                Log.d("MainActivity", "signInWithCredential:success")
                Toast.makeText(baseContext, "로그인에 성공하셨습니다.", Toast.LENGTH_SHORT).show()
                startSignIn()
            } else {
                val errorCode: String =
                    mOAuthLoginModule.getLastErrorCode(baseContext).code
                val errorDesc: String = mOAuthLoginModule.getLastErrorDesc(baseContext)
                Toast.makeText(
                    baseContext, "errorCode:" + errorCode
                            + ", errorDesc:" + errorDesc, Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    public override fun onStart() {
        // 어플을 실행할 때 마다 이전에 저장이 되었던 비밀번호 재설정 데이터를 초기화
        super.onStart()
        Log.d(TAG, "LoginActivity가 실행되었습니다.")
        val sharedPreference =
            getSharedPreferences("file name", MODE_PRIVATE)
        val editor = sharedPreference.edit()
        editor.clear()
        editor.apply()
        Log.d(TAG, "LoginActivity - onStart() called")
    }


    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, GOOGLE_REQUEST_CODE)
    }

    private fun signIn(email: String, password: String) {

        if (email.isNotEmpty() && password.isNotEmpty()) {
            auth?.signInWithEmailAndPassword(email, password)
                ?.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        if (checkAuth()) {
                            Toast.makeText(
                                baseContext, "로그인에 성공 하였습니다.",
                                Toast.LENGTH_SHORT
                            ).show()

                            val user = auth!!.currentUser
                            loginSuccess(user)
                            val sharedPreference =
                                getSharedPreferences("file name", MODE_PRIVATE)
                            val editor = sharedPreference.edit()
                            editor.putString("id", email)
                            editor.apply()
                        } else {
                            Toast.makeText(
                                baseContext, "전송된 메일로 이메일 인증이 되지 않았습니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            baseContext, "로그인에 실패 하였습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                        val sharedPreference =
                            getSharedPreferences("file name", MODE_PRIVATE)
                        val editor = sharedPreference.edit()
                        editor.putString("id", email)
                        editor.apply()
                    }
                }
        } else {
            Toast.makeText(
                baseContext, "로그인 항목에 빈칸이 존재합니다.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun facebookLogin() {
        LoginManager.getInstance()
            .logInWithReadPermissions(this, listOf("public_profile", "email"))
        LoginManager.getInstance()
            .registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult?) {
                    handleFBToken(result?.accessToken)
                }

                override fun onCancel() {
                    dialog("fail")
                }

                override fun onError(error: FacebookException?) {
                }
            })
    }

    private fun twitterLogin() {
        twitterAuthClient.authorize(this, object : Callback<TwitterSession>() {
            override fun success(result: com.twitter.sdk.android.core.Result<TwitterSession>?) {
                if (result != null) {
                    handleTwitterSession(result.data)
//                    twitterSignInBtn.text = "로그아웃"
                    loginstate = !loginstate
                }
            }

            override fun failure(exception: TwitterException?) {
                Toast.makeText(baseContext, "트위터 로그인에 실패하셨습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun startMainActivity() {
        Log.d(TAG, "LoginActivity - startMainActivity() called")
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun kakaoLoginStart() {
        Log.d(TAG, "LoginActivity - kakaoLoginStart() called")
        val keyHash = Utility.getKeyHash(this)
        // keyHash 발급
        Log.d(TAG, "KEY_HASH : $keyHash")
        Session.getCurrentSession().addCallback(callback)
        Session.getCurrentSession().open(AuthType.KAKAO_LOGIN_ALL, this)
    }

    private fun startSignIn() {
        accessToken?.let {
            auth!!.signInWithCustomToken(it)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCustomToken:success")
                        val user = auth!!.currentUser
                        loginSuccess(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCustomToken:failure", task.exception)
                        Toast.makeText(
                            baseContext, "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                        loginSuccess(null)
                    }
                }
        }
    }

    private fun handleTwitterSession(session: TwitterSession) {
        Log.d("MainActivity", "handleTwitterSession:$session")
        val credential = TwitterAuthProvider.getCredential(
            session.authToken.token,
            session.authToken.secret
        )

        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("MainActivity", "signInWithCredential:success")
                    Toast.makeText(baseContext, "로그인에 성공하셨습니다.", Toast.LENGTH_SHORT).show()
                    val user = auth!!.currentUser
                    loginSuccess(user)

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("MainActivity", "signInWithCredential:failure", task.exception)
                    Toast.makeText(baseContext, "로그인에 실패하였습니다.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager?.onActivityResult(requestCode, resultCode, data)
        twitterAuthClient.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == GOOGLE_REQUEST_CODE) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                dialog("fail")
                Log.w(TAG, "Google sign in failed", e)
                Toast.makeText(this, "로그인 실패", Toast.LENGTH_SHORT).show()
            }
        }

        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            Log.i(TAG, "Session get current session")
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "로그인 성공")
                    Toast.makeText(
                        baseContext, "로그인에 성공 하였습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                    val user = auth!!.currentUser
                    loginSuccess(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    dialog("fail")
                }
            }
    }

    private fun handleFBToken(token: AccessToken?) {
        val credential = FacebookAuthProvider.getCredential(token?.token!!)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "로그인 성공")
                    Toast.makeText(
                        baseContext, "로그인에 성공 하였습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                    val user = auth!!.currentUser
                    loginSuccess(user)
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    dialog("fail")
                }
            }
    }

    fun getFirebaseJwt(kakaoAccessToken: String): Task<String> {
        Log.d(TAG, "LoginActivity - getFirebaseJwt() called")
        val source = TaskCompletionSource<String>()
        val queue = Volley.newRequestQueue(this)
        val url = "http://로컬 IP:8000/verifyToken" // validation server
        val validationObject: HashMap<String?, String?> = HashMap()
        validationObject["token"] = kakaoAccessToken
        val request: JsonObjectRequest = object : JsonObjectRequest(
            Request.Method.POST, url,
            JSONObject(validationObject as Map<*, *>),
            Response.Listener { response ->
                try {
                    val firebaseToken = response.getString("firebase_token")
                    source.setResult(firebaseToken)
                } catch (e: Exception) {
                    source.setException(e)
                }
            },
            Response.ErrorListener { error ->
                Log.e(
                    TAG,
                    error.toString()
                )
                source.setException(error)
            }) {
            override fun getParams(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["Authorization"] = String.format(
                    "Basic %s",
                    Base64.encodeToString(
                        String.format("%s:%s", "token", kakaoAccessToken).toByteArray(),
                        Base64.DEFAULT
                    )
                )
                return params
            }
        }
        queue.add(request)
        return source.task
    }


    private fun loginSuccess(user: FirebaseUser?) {
        if (user != null) {
            Handler().postDelayed({
                val intent = Intent(this, MainActivity::class.java)
                intent.action = Intent.ACTION_MAIN
                intent.addCategory(Intent.CATEGORY_LAUNCHER)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }, 2000L)
        }
    }

    private fun checkAuth(): Boolean {
        val currentUser = auth?.currentUser
        return currentUser?.let {
            email = currentUser.email
            if (currentUser.isEmailVerified) {
                true
            } else {
                false
            }
        } ?: let {
            false
        }
    }

    private fun sendPasswordReset() {
        val sharedPreference = getSharedPreferences("file name", Context.MODE_PRIVATE)
        val savedId = sharedPreference.getString("id", "")

        if (savedId != "") {
            if (savedId != null) {
                Firebase.auth.sendPasswordResetEmail(savedId)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                baseContext, "비밀번호 재설정 이메일이 전송되었습니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.d(TAG, "Email sent.")
                        } else {
                            Toast.makeText(
                                baseContext, "비밀번호 재설정 이메일 전송이 실패했습니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.d(TAG, "Email is not sent.")
                        }
                    }
            }
        } else {
            Toast.makeText(
                baseContext, "비밀번호 재설정에 실패했습니다.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // 로그인 성공/실패 시 다이얼로그를 띄워주는 메소드
    fun dialog(type: String) {
        val dialog = AlertDialog.Builder(this)

        if (type.equals("fail")) {
            dialog.setTitle("로그인 실패")
            dialog.setMessage("아이디와 비밀번호를 확인해주세요")
        } else if (type.equals("empty")) {
            dialog.setTitle("회원정보 존재하지 않음")
            dialog.setTitle("아이디 비밀번호를 만들어주세요!")
        }

        val dialog_listener = object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface?, which: Int) {
                when (which) {
                    DialogInterface.BUTTON_POSITIVE ->
                        Log.d(TAG, "")
                }
            }
        }

        dialog.setPositiveButton("확인", dialog_listener)
        dialog.show()
    }

    override fun onBackPressed() {
        second_time = System.currentTimeMillis()
        if (second_time - first_time < 2000) {
            super.onBackPressed()
            finishAffinity()
        } else {
            Toast.makeText(this@LoginActivity, "뒤로가기를 한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT)
                .show()
        }
        first_time = System.currentTimeMillis()
    }

    override fun onDestroy() {
        super.onDestroy()
        Session.getCurrentSession().removeCallback(callback)
    }
}