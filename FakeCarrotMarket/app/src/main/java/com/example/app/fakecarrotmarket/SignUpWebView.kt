package com.example.app.fakecarrotmarket

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import androidx.appcompat.app.ActionBar

class SignUpWebView : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up_web_view)

        var actionBar : ActionBar? = supportActionBar
        actionBar?.hide()

        val backButton = findViewById<Button>(R.id.goBack)
        val webView = findViewById<WebView>(R.id.signup_webView)
        val setting = webView.getSettings()
        setting.setDefaultTextEncodingName("EUC-KR")
        webView.webViewClient = WebViewClient()
        webView.loadUrl("https://firebasestorage.googleapis.com/v0/b/first-project-df1fb.appspot.com/o/texts%2F%EC%95%BD%EA%B4%80%EB%8F%99%EC%9D%98%EC%84%9C.txt?alt=media&token=44fa86c4-4369-455e-aed4-de3a4f48fad8")

        backButton.setOnClickListener {
            onBackPressed()
        }
    }
}
