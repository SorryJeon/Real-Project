class MainActivity : AppCompatActivity() {
    lateinit var twitterAuthClient: TwitterAuthClient
    private lateinit var auth: FirebaseAuth
    var loginState = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Configure Twitter SDK
        val authConfig = TwitterAuthConfig(
            getString(R.string.twitter_consumer_key),
            getString(R.string.twitter_consumer_secret)
        )
 
        val twitterConfig = TwitterConfig.Builder(this)
            .twitterAuthConfig(authConfig)
            .build()
 
        Twitter.initialize(twitterConfig )
        setContentView(R.layout.activity_main)
 
        auth = FirebaseAuth.getInstance()
 
        twitterAuthClient = TwitterAuthClient()
 
        twitterSignButton.setOnClickListener {
            if (!loginState) {
                twitterLogin()
            } else {
                loginState = !loginState
                twitterSignButton.text = "트위터로그인"
                signOut()
            }
        }
    }
 
    private fun twitterLogin() {
        twitterAuthClient?.authorize(this, object : Callback<TwitterSession>() {
            override fun success(result: Result<TwitterSession>?) {
                if (result != null) {
                    handleTwitterSession(result.data)
                    toast("트위터 로그인성공")
                    twitterSignButton.text = "로그아웃"
                    loginState = !loginState
                }
            }
 
            override fun failure(exception: TwitterException?) {
                toast("트위터 로그인실패")
            }
        })
    }
 
    private fun handleTwitterSession(session: TwitterSession) {
        Log.d("MainActivity", "handleTwitterSession:$session")
        val credential = TwitterAuthProvider.getCredential(
            session.authToken.token,
            session.authToken.secret
        )
 
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("MainActivity", "signInWithCredential:success")
                    val user = auth.currentUser
                    loginInfo.text="id : ${user?.displayName}"
 
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("MainActivity", "signInWithCredential:failure", task.exception)
                    toast("Authentication failed.")
                }
            }
    }
 
    private fun signOut() {
        auth.signOut()
        TwitterCore.getInstance().sessionManager.clearActiveSession()
        loginInfo.text="info"
 
    }
 
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        twitterAuthClient?.onActivityResult(requestCode, resultCode, data)
    }
 
    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        loginInfo.text=currentUser?.displayName
    }
}