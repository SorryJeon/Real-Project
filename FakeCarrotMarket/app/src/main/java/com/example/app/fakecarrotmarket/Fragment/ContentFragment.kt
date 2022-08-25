package com.example.app.fakecarrotmarket

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.app.fakecarrotmarket.databinding.FragmentContentBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerView

class ContentFragment : Fragment(), YouTubePlayer.OnInitializedListener {

    private var playerView: YouTubePlayerView? = null
    private var player: YouTubePlayer? = null
    private lateinit var googleSignInClient: GoogleSignInClient
    private var binding: FragmentContentBinding? = null
    private val API_KEY = "AIzaSyBZFm1gePlZTWd6Q-uYnOsK1Ya4cf_hVvQ"
    private val videoId = "hl-ii7W4ITg"

//    private var firebaseDatabase = FirebaseDatabase.getInstance()
//    private var databaseReference = firebaseDatabase.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view: View = inflater.inflate(
            R.layout.fragment_content,
            container,
            false
        )

        // Inflate the layout for this fragment
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentContentBinding = FragmentContentBinding.bind(view)
        binding = fragmentContentBinding
        initPlayer()
    }

//    private fun playVideo() {
//        if (player != null) {
//            if (player!!.isPlaying) {
//                player?.pause()
//            }
//            player?.cueVideo(videoId)
//        }
//    }

    private fun initPlayer() {
        playerView = view?.findViewById(R.id.youtubePlayerView)
        playerView!!.initialize(API_KEY, this)
    }

    override fun onInitializationSuccess(
        provider: YouTubePlayer.Provider?,
        player: YouTubePlayer?,
        wasRestored: Boolean
    ) {
        if (player == null) return
        if (wasRestored) {
            player.play()
        } else {
            player.cueVideo(videoId)
            player.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT)
        }
    }

    override fun onInitializationFailure(
        p0: YouTubePlayer.Provider?,
        p1: YouTubeInitializationResult?
    ) {
        Log.d("Youtube Player", "Failed to initialize")
    }
}