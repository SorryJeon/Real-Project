class LoginActivity: AppCompatActivity() {
      private var callback: SessionCallback = SessionCallback()

      override fun onCreate(savedInstanceState: Bundle?){
          ...

          // Get hash key and print
          ...

		  // 카카오 제공 버튼일 경우
          Session.getCurrentSession.addCallback(callback);
      }
    
      override fun onDestroy() {
          ...
          Session.getCurrentSession().removeCallback(callback);
      }

      override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
          if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
              Dlog.d("session get current session")
              return
          }

          super.onActivityResult(requestCode, resultCode, data)
      }
  
    	private class SessionCallback : ISessionCallback {
          override fun onSessionOpenFailed(exception: KakaoException?) {
              Dlog.e("Session Call back :: onSessionOpenFailed: ${exception?.message}")
          }

          override fun onSessionOpened() {
              UserManagement.getInstance().me(object : MeV2ResponseCallback() {

                  override fun onFailure(errorResult: ErrorResult?) {
                      Dlog.d("Session Call back :: on failed ${errorResult?.errorMessage}")
                  }

                  override fun onSessionClosed(errorResult: ErrorResult?) {
                      Dlog.e("Session Call back :: onSessionClosed ${errorResult?.errorMessage}")

                  }

                  override fun onSuccess(result: MeV2Response?) {
                      checkNotNull(result) { "session response null" }
                      // register or login
                  }

              })
          }

    }