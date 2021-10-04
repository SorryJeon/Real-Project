package com.example.app.fakecarrotmarket

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_signup.*
import kotlinx.android.synthetic.main.activity_signup.view.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.jar.Manifest

class SignUpActivity : AppCompatActivity() {

    private var auth: FirebaseAuth? = null
    private var viewProfile: View? = null
    var fbStorage: FirebaseStorage? = null
    var pickImageFromAlbum = 0
    var uriPhoto: Uri? = null
    val TAG: String = "Register"
    var isExistBlank = false
    var isPWSame = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        setContentView(R.layout.activity_signup)
        val btn_register = findViewById<Button>(R.id.btn_register)
        val btn_cancel = findViewById<Button>(R.id.btn_cancel)
        val edit_id = findViewById<EditText>(R.id.edit_id)
        val edit_pw = findViewById<EditText>(R.id.edit_pw)
        val edit_pw_re = findViewById<EditText>(R.id.edit_pw_re)

        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1)

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

                // 회원가입 성공 토스트 메세지 띄우기
                Toast.makeText(this, "회원가입 성공", Toast.LENGTH_SHORT).show()

                // 유저가 입력한 id, pw를 쉐어드에 저장한다.
                val sharedPreference = getSharedPreferences("file name", MODE_PRIVATE)
                val editor = sharedPreference.edit()
                editor.putString("id", id)
                editor.putString("pw", pw)
                editor.apply()

                auth?.createUserWithEmailAndPassword(id, pw)
                        ?.addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(
                                        this, "계정 생성 완료.",
                                        Toast.LENGTH_SHORT
                                ).show()
                                val intent = Intent(this, LoginActivity::class.java)
                                startActivity(intent) // 가입창 종료, 로그인 화면으로 이동
                            } else {
                                Toast.makeText(
                                        this, "계정 생성 실패",
                                        Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

            } else {

                // 상태에 따라 다른 다이얼로그 띄워주기
                if (isExistBlank) {   // 작성 안한 항목이 있을 경우
                    val sharedPreference = getSharedPreferences("file name", MODE_PRIVATE)
                    val editor = sharedPreference.edit()
                    editor.putString("id", id)
                    editor.putString("pw", pw)
                    editor.clear()
                    editor.apply()
                    dialog("blank")
                } else if (!isPWSame) { // 입력한 비밀번호가 다를 경우
                    val sharedPreference = getSharedPreferences("file name", MODE_PRIVATE)
                    val editor = sharedPreference.edit()
                    editor.putString("id", id)
                    editor.putString("pw", pw)
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewProfile = inflater?.inflate(R.layout.activity_signup, container, false)
        fbStorage = FirebaseStorage.getInstance()
        viewProfile!!.btn_image.setOnClickListener {
            // open Album
            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            startActivityForResult(photoPickerIntent, pickImageFromAlbum)
        }
        return viewProfile
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == pickImageFromAlbum) {
            if (resultCode == Activity.RESULT_OK) {
                uriPhoto = data?.data
                xml_image.setImageURI(uriPhoto)

                if (ContextCompat.checkSelfPermission(viewProfile!!.context, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    funImageUpload(viewProfile!!)
                }
            } else {

            }
        }
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

    private fun funImageUpload(view: View) {
        var timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imgFileName = "IMAGE_" + timeStamp + "_.png"
        var storageRef = fbStorage?.reference?.child("images")?.child(imgFileName)

        storageRef?.putFile(uriPhoto!!)?.addOnSuccessListener {
            Toast.makeText(view.context, "Image Uploaded", Toast.LENGTH_SHORT).show()
        }
    }
}