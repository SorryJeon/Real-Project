package com.example.app.fakecarrotmarket

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.AuthErrorCause
import com.kakao.sdk.user.UserApiClient
import com.twitter.sdk.android.core.*
import com.twitter.sdk.android.core.identity.TwitterAuthClient
import kotlinx.android.synthetic.main.activity_login.*
import kotlin.Result
import kotlin.math.log
import com.google.firebase.auth.OAuthProvider
import androidx.annotation.NonNull

import com.google.android.gms.tasks.OnFailureListener

import com.google.firebase.auth.AuthResult

import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.ktx.oAuthProvider
import com.google.firebase.auth.FirebaseUser


class LoginActivity : AppCompatActivity() {
    lateinit var twitterAuthClient: TwitterAuthClient
    var loginstate = false
    var first_time: Long = 0
    var second_time: Long = 0
    var auth: FirebaseAuth? = null
    val GOOGLE_REQUEST_CODE = 99
    val TAG = "googleLogin"
    var email: String? = null
    var callbackManager: CallbackManager? = null

    private lateinit var googleSignInClient: GoogleSignInClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val authConfig = TwitterAuthConfig(
            getString(R.string.twitter_consumer_key),
            getString(R.string.twitter_consumer_secret)
        )

        val twitterConfig = TwitterConfig.Builder(this)
            .twitterAuthConfig(authConfig)
            .build()

        Twitter.initialize(twitterConfig)

        twitterAuthClient = TwitterAuthClient()

        UserApiClient.instance.accessTokenInfo { tokenInfo, error ->
            if (error != null) {

            } else if (tokenInfo != null) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                finish()
            }
        }

        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                when {
                    error.toString() == AuthErrorCause.AccessDenied.toString() -> {
                        Toast.makeText(this, "접근이 거부 됨(동의 취소)", Toast.LENGTH_SHORT).show()
                    }
                    error.toString() == AuthErrorCause.InvalidClient.toString() -> {
                        Toast.makeText(this, "유효하지 않은 앱", Toast.LENGTH_SHORT).show()
                    }
                    error.toString() == AuthErrorCause.InvalidGrant.toString() -> {
                        Toast.makeText(this, "인증 수단이 유효하지 않아 인증할 수 없는 상태", Toast.LENGTH_SHORT)
                            .show()
                    }
                    error.toString() == AuthErrorCause.InvalidRequest.toString() -> {
                        Toast.makeText(this, "요청 파라미터 오류", Toast.LENGTH_SHORT).show()
                    }
                    error.toString() == AuthErrorCause.InvalidScope.toString() -> {
                        Toast.makeText(this, "유효하지 않은 scope ID", Toast.LENGTH_SHORT).show()
                    }
                    error.toString() == AuthErrorCause.Misconfigured.toString() -> {
                        Toast.makeText(this, "설정이 올바르지 않음(android key hash)", Toast.LENGTH_SHORT)
                            .show()
                    }
                    error.toString() == AuthErrorCause.ServerError.toString() -> {
                        Toast.makeText(this, "서버 내부 에러", Toast.LENGTH_SHORT).show()
                    }
                    error.toString() == AuthErrorCause.Unauthorized.toString() -> {
                        Toast.makeText(this, "앱이 요청 권한이 없음", Toast.LENGTH_SHORT).show()
                    }
                    else -> { // Unknown
                        Toast.makeText(this, "카카오 로그인 실패", Toast.LENGTH_SHORT).show()
                    }
                }
            } else if (token != null) {
                Toast.makeText(this, "로그인에 성공하였습니다.", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                finish()
            }
        }

        val googleSignInBtn = findViewById<Button>(R.id.googleSignInBtn)
        val facebookSignInBtn = findViewById<Button>(R.id.facebookSignInBtn)
        val kakaoSignInBtn = findViewById<Button>(R.id.kakaoSignInBtn)
        val twitterSignInBtn = findViewById<Button>(R.id.twitterSignInBtn)
        val githubSignInBtn = findViewById<Button>(R.id.githubSignInBtn)

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
            if (UserApiClient.instance.isKakaoTalkLoginAvailable(baseContext)) {
                UserApiClient.instance.loginWithKakaoTalk(baseContext, callback = callback)
            } else {
                UserApiClient.instance.loginWithKakaoAccount(baseContext, callback = callback)
            }
        }

        twitterSignInBtn.setOnClickListener {
            if (!loginstate) {
                twitterLogin()
            } else {
                loginstate = !loginstate
                twitterSignInBtn.text = "트위터로그인"
                signOut()
            }
        }

        githubSignInBtn.setOnClickListener {
            githubLogin()
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

    override fun onStart() {
        // 어플을 실행할 때 마다 이전에 저장이 되었던 비밀번호 재설정 데이터를 초기화
        val sharedPreference =
            getSharedPreferences("file name", MODE_PRIVATE)
        val editor = sharedPreference.edit()
        editor.clear()
        editor.apply()
        super.onStart()
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
                    twitterSignInBtn.text = "로그아웃"
                    loginstate = !loginstate
                }
            }

            override fun failure(exception: TwitterException?) {
                Toast.makeText(baseContext, "트위터 로그인에 실패하셨습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun githubLogin() {

        val provider = OAuthProvider.newBuilder("github.com")
        provider.addCustomParameter("login", "your-email@gmail.com")
        auth!!
            .startActivityForSignInWithProvider( /* activity= */this, provider.build())
            .addOnSuccessListener(
                OnSuccessListener<AuthResult?> {
                    Toast.makeText(baseContext, "로그인에 성공하셨습니다.", Toast.LENGTH_SHORT).show()
                    val user = auth!!.currentUser
                    loginSuccess(user)
                    // User is signed in.
                    // IdP data available in
                    // authResult.getAdditionalUserInfo().getProfile().
                    // The OAuth access token can also be retrieved:
                    // authResult.getCredential().getAccessToken().
                })
            .addOnFailureListener(
                OnFailureListener {
                    Toast.makeText(baseContext, "로그인에 실패하였습니다.", Toast.LENGTH_SHORT).show()
                    // Handle failure.
                })
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
                    loginInfo.text = "id : ${user?.displayName}"

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("MainActivity", "signInWithCredential:failure", task.exception)
                    Toast.makeText(baseContext, "로그인에 실패하였습니다.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun signOut() {
        auth?.signOut()
        TwitterCore.getInstance().sessionManager.clearActiveSession()
        loginInfo.text = "info"
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager?.onActivityResult(requestCode, resultCode, data)
        twitterAuthClient?.onActivityResult(requestCode, resultCode, data)

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
        var dialog = AlertDialog.Builder(this)

        if (type.equals("fail")) {
            dialog.setTitle("로그인 실패")
            dialog.setMessage("아이디와 비밀번호를 확인해주세요")
        } else if (type.equals("empty")) {
            dialog.setTitle("회원정보 존재하지 않음")
            dialog.setTitle("아이디 비밀번호를 만들어주세요!")
        }

        var dialog_listener = object : DialogInterface.OnClickListener {
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
            Toast.makeText(this@LoginActivity, "뒤로가기를 한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()
        }
        first_time = System.currentTimeMillis()
    }
}