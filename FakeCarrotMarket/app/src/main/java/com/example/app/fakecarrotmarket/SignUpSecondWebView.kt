package com.example.app.fakecarrotmarket

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button

class SignUpSecondWebView : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up_second_web_view)

        val backButton = findViewById<Button>(R.id.goBack)
        val webView = findViewById<WebView>(R.id.signup_webView)
        val setting = webView.getSettings()
        setting.setDefaultTextEncodingName("EUC-KR")
        webView.webViewClient = WebViewClient()
        webView.loadUrl("https://firebasestorage.googleapis.com/v0/b/first-project-df1fb.appspot.com/o/texts%2F%EA%B0%9C%EC%9D%B8%EC%A0%95%EB%B3%B4%EC%B7%A8%EA%B8%89%EB%B0%A9%EC%B9%A8.txt?alt=media&token=426ef5c5-fab7-4a0b-a45f-7aaa996f3006")

        backButton.setOnClickListener {
            onBackPressed()
        }
    }
}