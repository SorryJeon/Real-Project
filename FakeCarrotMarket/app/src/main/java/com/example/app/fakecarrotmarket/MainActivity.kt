package com.example.app.fakecarrotmarket

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), View.OnClickListener {
    var btnRevoke: Button? = null
    var btnLogout: Button? = null
    var btnexit: Button? = null
    var auth: FirebaseAuth? = null
    var fbFireStore: FirebaseFirestore? = null

    private lateinit var googleSignInClient: GoogleSignInClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnLogout = findViewById<View>(R.id.btn_logout) as Button
        btnRevoke = findViewById<View>(R.id.btn_revoke) as Button
        btnexit = findViewById<View>(R.id.btn_exit) as Button
        auth = FirebaseAuth.getInstance()
        fbFireStore = FirebaseFirestore.getInstance()

        btnLogout!!.setOnClickListener(this)
        btnRevoke!!.setOnClickListener(this)
        btnexit!!.setOnClickListener(this)

        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
            1
        )

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

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_logout -> {
                signOut()
                finishAffinity()
            }
            R.id.btn_revoke -> {
                revokeAccess()
                finishAffinity()
            }
            R.id.btn_exit -> {
                finishAffinity()
            }
        }
    }
}