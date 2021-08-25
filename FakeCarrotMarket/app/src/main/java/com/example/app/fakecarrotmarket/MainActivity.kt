package com.example.app.fakecarrotmarket


import androidx.appcompat.app.AppCompatActivity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView

import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider


class MainActivity : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private var signInButton: SignInButton? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val tvContents = findViewById<View>(R.id.tv_contents) as TextView
        val ivGlide = findViewById<View>(R.id.iv_glide) as ImageView
        signInButton = findViewById(R.id.signInButton)
        Glide.with(this)
            .load("http://goo.gl/gEgYUd")
            .override(300, 200)
            .fitCenter()
            .into(ivGlide)
        mAuth = FirebaseAuth.getInstance()
        if (mAuth!!.currentUser != null) {
            val intent = Intent(application, AfterActivity::class.java)
            startActivity(intent)
            finish()
        }
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        signInButton?.setOnClickListener(View.OnClickListener { signIn() })
    }

    // [START signin]
    private fun signIn() {
        val signInIntent = mGoogleSignInClient!!.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult<ApiException>(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(
                this
            ) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Snackbar.make(
                        findViewById(R.id.layout_main),
                        "Authentication Successed.",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    val user = mAuth!!.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Snackbar.make(
                        findViewById(R.id.layout_main),
                        "Authentication Failed.",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    updateUI(null)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) { //update ui code here
        if (user != null) {
            val intent = Intent(this, AfterActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    companion object {
        private const val RC_SIGN_IN = 9001
    }
}