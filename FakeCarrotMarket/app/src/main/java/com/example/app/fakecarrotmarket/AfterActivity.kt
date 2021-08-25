package com.example.app.fakecarrotmarket

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth


class AfterActivity : AppCompatActivity(), View.OnClickListener {
    var btnRevoke: Button? = null
    var btnLogout: Button? = null
    private var mAuth: FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_after)
        btnLogout = findViewById<View>(R.id.btn_logout) as Button
        btnRevoke = findViewById<View>(R.id.btn_revoke) as Button
        mAuth = FirebaseAuth.getInstance()
        btnLogout!!.setOnClickListener(this)
        btnRevoke!!.setOnClickListener(this)
    }

    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
    }

    private fun revokeAccess() {
        mAuth!!.currentUser!!.delete()
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