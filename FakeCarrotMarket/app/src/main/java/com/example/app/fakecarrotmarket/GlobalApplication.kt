package com.example.app.fakecarrotmarket

import android.app.Application
import com.kakao.auth.KakaoSDK

class GlobalApplication : Application() {

    companion object {
        var instance: GlobalApplication? = null
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        if (KakaoSDK.getAdapter() == null) {
            KakaoSDK.init(KakaoSDKAdapter(getAppContext()))
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        instance = null
    }

    fun getAppContext(): GlobalApplication {
        checkNotNull(instance) { "This Application does not inherit com.example.App" }
        return instance!!
    }
}
