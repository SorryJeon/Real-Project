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
import bolts.Task.delay
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {
    var first_time: Long = 0
    var second_time: Long = 0
    var auth: FirebaseAuth? = null
    val GOOGLE_REQUEST_CODE = 99
    val TAG = "googleLogin"
    var callbackManager: CallbackManager? = null

    private lateinit var googleSignInClient: GoogleSignInClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val googleSignInBtn = findViewById<Button>(R.id.googleSignInBtn)
        val facebookSignInBtn = findViewById<Button>(R.id.facebookSignInBtn)

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

//        btn_clear.setOnClickListener {
//            val sharedPreference = getSharedPreferences("file name", Context.MODE_PRIVATE)
//            val editor = sharedPreference.edit()
//            editor.clear()
//            editor.apply()
//            dialog("clear")
//        }
    }

    override fun onStart() {
        super.onStart()
        val sharedPreference = getSharedPreferences("file name", MODE_PRIVATE)
        val editor = sharedPreference.edit()
        editor.clear()
        editor.apply()
//        val currentUser = auth?.currentUser
//        if (currentUser != null) {
//            dialog("exist")
//            delay(2000)
//            loginSuccess(currentUser)
//        }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager?.onActivityResult(requestCode, resultCode, data)

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

    private fun sendPasswordReset() {
        val sharedPreference = getSharedPreferences("file name", Context.MODE_PRIVATE)
        val savedId = sharedPreference.getString("id", "")

        if (savedId != "") {
            if (savedId != null) {
                Firebase.auth.sendPasswordResetEmail(savedId)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "Email sent.")
                        } else {
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
//        else if (type.equals("exist")) {
//            dialog.setTitle("이미 로그인된 상태")
//            dialog.setTitle("잠시후 다음 화면으로 넘어갑니다!")
//        }


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