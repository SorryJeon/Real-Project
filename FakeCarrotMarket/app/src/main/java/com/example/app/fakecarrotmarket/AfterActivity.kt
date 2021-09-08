package com.example.app.fakecarrotmarket

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth


class AfterActivity : AppCompatActivity(), View.OnClickListener {
    var btnRevoke: Button? = null
    var btnLogout: Button? = null
    private var mAuth: FirebaseAuth? = null
    private lateinit var googleSignInClient: GoogleSignInClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_after)
        btnLogout = findViewById<View>(R.id.btn_logout) as Button
        btnRevoke = findViewById<View>(R.id.btn_revoke) as Button
        mAuth = FirebaseAuth.getInstance()
        btnLogout!!.setOnClickListener(this)
        btnRevoke!!.setOnClickListener(this)

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
        if (account!==null) {
            googleSignInClient.signOut().addOnCompleteListener(this) {
                //updateUI(null)
            }
        }
    }

    private fun revokeAccess() {
        val account = GoogleSignIn.getLastSignedInAccount(this)
        mAuth!!.currentUser!!.delete()
        LoginManager.getInstance().logOut()
        if (account!==null) {
            googleSignInClient.revokeAccess().addOnCompleteListener(this) {

            }
        }

    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_logout -> {
                signOut()
                finishAffinity()
    2        }
            R.id.btn_revoke -> {
                revokeAccess()
                finishAffinity()
            }
        }
    }
    //    btn_register2.setOnClickListener {
    //         Log.d(TAG, "회원가입 버튼 클릭")

    //         val id = edit_id.text.toString()
    //         val pw = edit_pw.text.toString()
    //         val pw_re = edit_pw_re.text.toString()

    //         // 유저가 항목을 다 채우지 않았을 경우
    //         if(id.isEmpty() || pw.isEmpty() || pw_re.isEmpty()){
    //             isExistBlank = true
    //         }
    //         else{
    //             if(pw == pw_re){
    //                 isPWSame = true
    //             }
    //         }

    //         if(!isExistBlank && isPWSame){

    //             // 회원가입 성공 토스트 메세지 띄우기
    //             Toast.makeText(this, "회원가입 성공", Toast.LENGTH_SHORT).show()

    //             // 유저가 입력한 id, pw를 쉐어드에 저장한다.
    //             val sharedPreference = getSharedPreferences("file name", Context.MODE_PRIVATE)
    //             val editor = sharedPreference.edit()
    //             editor.putString("id", id)
    //             editor.putString("pw", pw)
    //             editor.apply()

    //             // 로그인 화면으로 이동
    //             val intent = Intent(this, MainActivity::class.java)
    //             startActivity(intent)

    //         }
    //         else{

    //             // 상태에 따라 다른 다이얼로그 띄워주기
    //             if(isExistBlank){   // 작성 안한 항목이 있을 경우
    //                 dialog("blank")
    //             }
    //             else if(!isPWSame){ // 입력한 비밀번호가 다를 경우
    //                 dialog("not same")
    //             }
    //         }

    //     }
    // }

    // // 회원가입 실패시 다이얼로그를 띄워주는 메소드
    // fun dialog(type: String){
    //     val dialog = AlertDialog.Builder(this)

    //     // 작성 안한 항목이 있을 경우
    //     if(type.equals("blank")){
    //         dialog.setTitle("회원가입 실패")
    //         dialog.setMessage("입력란을 모두 작성해주세요")
    //     }
    //     // 입력한 비밀번호가 다를 경우
    //     else if(type.equals("not same")){
    //         dialog.setTitle("회원가입 실패")
    //         dialog.setMessage("비밀번호가 다릅니다")
    //     }

    //     val dialog_listener = object: DialogInterface.OnClickListener{
    //         override fun onClick(dialog: DialogInterface?, which: Int) {
    //             when(which){
    //                 DialogInterface.BUTTON_POSITIVE ->
    //                     Log.d(TAG, "다이얼로그")
    //             }
    //         }
    //     }

    //     dialog.setPositiveButton("확인",dialog_listener)
    //     dialog.show()
    // }


}