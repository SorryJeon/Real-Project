package com.example.app.fakecarrotmarket

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.*
import androidx.appcompat.app.ActionBar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_signup.*

class SignUpActivity : AppCompatActivity() {

    lateinit var auth: FirebaseAuth
    val TAG: String = "Register"
    var isExistBlank = false
    var isPWSame = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        setContentView(R.layout.activity_signup)

        val actionBar: ActionBar? = supportActionBar
        supportActionBar?.setDisplayShowTitleEnabled(false)
        actionBar?.hide()

        val btn_register = findViewById<Button>(R.id.btn_register)
        val btn_cancel = findViewById<Button>(R.id.btn_cancel)
        val edit_id = findViewById<EditText>(R.id.edit_id)
        val edit_pw = findViewById<EditText>(R.id.edit_pw)
        val edit_pw_re = findViewById<EditText>(R.id.edit_pw_re)
        val allCheckBtn = findViewById<CheckBox>(R.id.allcheckbtn)
        val firstCheckBtn = findViewById<CheckBox>(R.id.firstcheckbtn)
        val secondCheckBtn = findViewById<CheckBox>(R.id.secondcheckbtn)
        val button1 = findViewById<Button>(R.id.button1)
        val button2 = findViewById<Button>(R.id.button2)

        allCheckBtn.setOnClickListener { onCheckChanged(allCheckBtn) }
        firstCheckBtn.setOnClickListener { onCheckChanged(firstCheckBtn) }
        secondCheckBtn.setOnClickListener { onCheckChanged(secondCheckBtn) }
        button1.setOnClickListener { clickButton1() }
        button2.setOnClickListener { clickButton2() }

        btn_register.setOnClickListener {
            Log.d(TAG, "회원가입 버튼 클릭")

            val id = edit_id.text.toString()
            val pw = edit_pw.text.toString()
            val pw_re = edit_pw_re.text.toString()
            val pattern = Patterns.EMAIL_ADDRESS

            // 유저가 항목을 다 채우지 않았을 경우
            if (id.isEmpty() || pw.isEmpty() || pw_re.isEmpty()) {
                isExistBlank = true
            } else {
                if (pw == pw_re) {
                    if (isPasswordFormat(pw)) {
                        isPWSame = true

                        if (pattern.matcher(id).matches()) {
                            isExistBlank = false
                        } else {
                            isExistBlank = true
                        }
                    } else {
                        isPWSame = false
                    }
                }
            }

            if (!isExistBlank && isPWSame) {

                if (allcheckbtn.isChecked) {
                    // 유저가 입력한 id, pw를 쉐어드에 저장한다.
                    val sharedPreference = getSharedPreferences("file name", MODE_PRIVATE)
                    val editor = sharedPreference.edit()

                    editor.putString("id", id)
                    editor.putString("pw", pw)
                    editor.apply()

                    auth.createUserWithEmailAndPassword(id, pw)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                auth.currentUser!!.sendEmailVerification()
                                    .addOnCompleteListener { sendTask ->
                                        if (sendTask.isSuccessful) {
                                            Toast.makeText(
                                                baseContext, " 전송된 메일을 확인해주세요.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            val intent = Intent(this, LoginActivity::class.java)
                                            startActivity(intent)
                                        } else {
                                            Toast.makeText(
                                                baseContext, "메일이 유효한지 확인해주세요.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                            } else {
                                Toast.makeText(
                                    baseContext, "이미 생성된 계정입니다.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                } else {
                    dialog("not check")
                }
            } else {

                // 상태에 따라 다른 다이얼로그 띄워주기
                if (isExistBlank) {   // 작성 안한 항목이 있을 경우
                    val sharedPreference = getSharedPreferences("file name", MODE_PRIVATE)
                    val editor = sharedPreference.edit()
                    editor.clear()
                    editor.apply()
                    dialog("blank")
                } else if (!isPWSame) { // 입력한 비밀번호가 다를 경우
                    val sharedPreference = getSharedPreferences("file name", MODE_PRIVATE)
                    val editor = sharedPreference.edit()
                    editor.clear()
                    editor.apply()
                    dialog("not same")
                }
            }

        }

        btn_cancel.setOnClickListener {
            val sharedPreference = getSharedPreferences("file name", MODE_PRIVATE)
            val editor = sharedPreference.edit()
            editor.clear()
            editor.apply() // 초기화 작업 하고 뒤로 가기
            Toast.makeText(
                this, "이전 화면으로 돌아갑니다.",
                Toast.LENGTH_SHORT
            ).show()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun onCheckChanged(compoundButton: CompoundButton) {
        when (compoundButton.id) {
            R.id.allcheckbtn -> {
                if (allcheckbtn.isChecked) {
                    firstcheckbtn.isChecked = true
                    secondcheckbtn.isChecked = true
                } else {
                    firstcheckbtn.isChecked = false
                    secondcheckbtn.isChecked = false
                }
            }
            else -> {
                allcheckbtn.isChecked = (
                        firstcheckbtn.isChecked
                                && secondcheckbtn.isChecked)
            }
        }
    }

    private fun clickButton1() {
        val intent = Intent(this, SignUpWebView::class.java)
        startActivity(intent)

    }

    private fun clickButton2() {
        val intent = Intent(this, SignUpSecondWebView::class.java)
        startActivity(intent)
    }

    fun isPasswordFormat(password: String): Boolean {
        return password.matches("^(?=.*[a-zA-Z0-9])(?=.*[a-zA-Z!@#\$%^&*])(?=.*[0-9!@#\$%^&*]).{6,15}\$".toRegex())
    }

    // 회원가입 실패시 다이얼로그를 띄워주는 메소드
    fun dialog(type: String) {
        val dialog = AlertDialog.Builder(this)

        // 작성 안한 항목이 있을 경우
        if (type.equals("blank")) {
            dialog.setTitle("회원가입 실패")
            dialog.setMessage("입력란을 모두 작성해주세요")
        }
        // 입력한 비밀번호가 다를 경우
        else if (type.equals("not same")) {
            dialog.setTitle("회원가입 실패")
            dialog.setMessage("비밀번호가 다릅니다")
        } else if (type.equals("not check")) {
            dialog.setTitle("약관동의 미체크")
            dialog.setMessage("약관동의 체크를 해주세요")
        }

        val dialog_listener = object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface?, which: Int) {
                when (which) {
                    DialogInterface.BUTTON_POSITIVE ->
                        Log.d(TAG, "다이얼로그")
                }
            }
        }

        dialog.setPositiveButton("확인", dialog_listener)
        dialog.show()
    }

    override fun onBackPressed() {
        Toast.makeText(
            this, "이전 화면으로 돌아갑니다.",
            Toast.LENGTH_SHORT
        ).show()
        super.onBackPressed()
    }
}