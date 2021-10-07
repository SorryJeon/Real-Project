package com.example.app.fakecarrotmarket

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    var btnRevoke: Button? = null
    var btnLogout: Button? = null
    var btnexit: Button? = null
    var btnimage: Button? = null
    var auth: FirebaseAuth? = null
    var fbFireStore: FirebaseFirestore? = null

    private var selectedUri: Uri? = null
    private lateinit var googleSignInClient: GoogleSignInClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnLogout = findViewById<View>(R.id.btn_logout) as Button
        btnRevoke = findViewById<View>(R.id.btn_revoke) as Button
        btnexit = findViewById<View>(R.id.btn_exit) as Button
        btnimage = findViewById<View>(R.id.btn_image) as Button
        auth = FirebaseAuth.getInstance()
        fbFireStore = FirebaseFirestore.getInstance()

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
            ImageFragment()
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
}