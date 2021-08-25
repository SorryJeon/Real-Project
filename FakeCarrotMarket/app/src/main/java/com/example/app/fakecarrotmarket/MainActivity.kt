package com.example.app.fakecarrotmarket


import android.content.Intent

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.facebook.*
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
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
import java.util.*


class MainActivity : AppCompatActivity() {
    private var mAuth: FirebaseAuth? = null
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private var signInButton: SignInButton? = null
    private var callbackManager: CallbackManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var facebookButton = findViewById<Button>(R.id.loginButton_facebook)
        facebookButton.setOnClickListener(View.OnClickListener {
            callbackManager = CallbackManager.Factory.create()
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "email"))
            LoginManager.getInstance().registerCallback(callbackManager,
                object : FacebookCallback<LoginResult> {
                    override fun onSuccess(loginResult: LoginResult) {
                        Log.d("MainActivity", "Facebook token: " + loginResult.accessToken.token)
                        startActivity(Intent(applicationContext, AfterActivity::class.java))
                    }

                    override fun onCancel() {
                        Log.d("MainActivity", "Facebook onCancel.")
                        startActivity(Intent(applicationContext, AfterActivity::class.java))
                    }

                    override fun onError(error: FacebookException?) {
                        Log.d("MainActivity", "Facebook onError.")
                        startActivity(Intent(applicationContext, AfterActivity::class.java))
                    }

                })
        })

        fun requestMe(accessToken: AccessToken) {
            val request = GraphRequest.newMeRequest(accessToken) { `object`, response ->
                try {
                    //here is the data that you want

                    val userEmail = `object`.getString("email")
                    Log.e("TAGG", userEmail)
                    val userName = `object`.getString("name")
                    Log.e("TAGG", userName)
                    val jobj1 = `object`.optJSONObject("picture")
                    Log.e("TAGG", jobj1.toString())
                    val jobj2 = jobj1.optJSONObject("data")
                    Log.e("TAGG", jobj2.toString())
                    val userPicture = jobj2.getString("url")
                    Log.e("TAGG", userPicture)



                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

            val parameters = Bundle()
            parameters.putString("fields", "name,email,picture")
            request.parameters = parameters
            request.executeAsync()

        }
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
        callbackManager?.onActivityResult(requestCode, resultCode, data)

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