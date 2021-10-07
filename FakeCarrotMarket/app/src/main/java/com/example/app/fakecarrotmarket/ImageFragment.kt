package com.example.app.fakecarrotmarket

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import java.text.SimpleDateFormat
import java.util.*

class ImageFragment : Fragment() {
    var fbAuth: FirebaseAuth? = null
    var fbFirestore: FirebaseFirestore? = null
    var fbStorage: FirebaseStorage? = null
    var uriPhoto: Uri? = null
    var pickImageFromAlbum = 0
    private var viewProfile: View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewProfile = inflater.inflate(R.layout.activity_main, container, false)
        fbStorage = FirebaseStorage.getInstance()
        fbAuth = FirebaseAuth.getInstance()
        fbFirestore = FirebaseFirestore.getInstance()

        viewProfile!!.btn_image.setOnClickListener {
            // open Album
            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            startActivityForResult(photoPickerIntent, pickImageFromAlbum)
        }
        return viewProfile
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == pickImageFromAlbum) {
            if (resultCode == Activity.RESULT_OK) {
                uriPhoto = data?.data
                xml_image.setImageURI(uriPhoto)

                if (ContextCompat.checkSelfPermission(
                        viewProfile!!.context,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    funImageUpload(viewProfile!!)
                }
            } else {

            }
        }
    }

    private fun funImageUpload(view: View) {
        var timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imgFileName = "IMAGE_" + timeStamp + "_.png"
        var storageRef = fbStorage?.reference?.child("images")?.child(imgFileName)

        storageRef?.putFile(uriPhoto!!)?.addOnSuccessListener {
            Toast.makeText(view.context, "Image Uploaded", Toast.LENGTH_SHORT).show()
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                var userInfo = ModelUsers()
                userInfo.imageUrl = uri.toString()

                fbFirestore?.collection("users")?.document(fbAuth?.uid.toString())
                    ?.update("imageUrl", userInfo.imageUrl.toString())
            }
        }
    }
}