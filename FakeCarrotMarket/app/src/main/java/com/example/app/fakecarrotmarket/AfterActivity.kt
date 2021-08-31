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
            .requestIdToken(getString(R.string.default_web_client_id))
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
            }
            R.id.btn_revoke -> {
                revokeAccess()
                finishAffinity()
            }
        }
    }
}