package com.example.app.fakecarrotmarket

import com.kakao.auth.*

class KakaoSDKAdapter(context: GlobalApplication) : KakaoAdapter() {
    override fun getApplicationConfig(): IApplicationConfig {
        return IApplicationConfig { GlobalApplication.instance?.getAppContext() }
    }

    override fun getSessionConfig(): ISessionConfig {
        return object : ISessionConfig {
            override fun getAuthTypes(): Array<AuthType> {
                return arrayOf(AuthType.KAKAO_LOGIN_ALL)
            }

            override fun isUsingWebviewTimer(): Boolean {
                return false
            }

            override fun isSecureMode(): Boolean {
                return true
            }

            override fun getApprovalType(): ApprovalType {
                return ApprovalType.INDIVIDUAL
            }

            override fun isSaveFormData(): Boolean {
                return true
            }
        }
    }
}