package com.ntech.chatapp.ui.activity

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.ntech.chatapp.R
import com.ntech.chatapp.util.Constants
import com.ntech.chatapp.util.Util
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.profile_content.*
import java.io.FileNotFoundException

class ProfileActivity : AppCompatActivity() {

    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val mDatabase: FirebaseDatabase by lazy { FirebaseDatabase.getInstance() }
    private val mStoreRef: StorageReference by lazy { FirebaseStorage.getInstance().reference }
    private val mCurrentUser: FirebaseUser by lazy { mAuth.currentUser!! }
    private lateinit var mUserReference: DatabaseReference
    private val GALLERY_REQUEST_CODE = 101
    private lateinit var dialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)


        setSupportActionBar(profile_page_toolbar)

        setUserData()

        change_image_fab.setOnClickListener {
            changeImage()
        }

        status_text.setOnClickListener {
            showStatusChangeDialog()
        }
    }

    private fun showStatusChangeDialog() {

        val dialogBuilder = AlertDialog.Builder(this)
        val dialogView: View = layoutInflater.inflate(R.layout.custom_dialog_view, null)

        dialogBuilder.setView(dialogView)
        val edt: EditText = dialogView.findViewById(R.id.dialog_status_text)
        dialogBuilder.setTitle("Change Status")
        dialogBuilder.setPositiveButton("Apply") { dialog, which ->
            val dialogStatusText = edt.text.toString()
            setStatusText(dialogStatusText)
        }
        
        dialogBuilder.setNegativeButton("Cancel") { dialog, which ->
            dialog.dismiss()
        }

        val dialog = dialogBuilder.create()
        dialog.show()
    }

    private fun setStatusText(status: String) {

        mDatabase.reference
            .child(Constants.CHILD_USERS)
            .child(mCurrentUser.uid)
            .child(Constants.CHILD_STATUS).setValue(status)
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    Toast.makeText(this, "Status Changed", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Failed", Toast.LENGTH_LONG).show()
                }
            }


    }

    private fun setUserData() {

        val userId = mCurrentUser.uid
        mUserReference = mDatabase.reference.child(Constants.CHILD_USERS).child(userId)


        mUserReference.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val name = snapshot.child(Constants.CHILD_NAME).value.toString()
                val profileImage = snapshot.child(Constants.CHILD_PROFILE_IMAGE).value.toString()
                val status = snapshot.child(Constants.CHILD_STATUS).value.toString()

                profile_page_collapsing.title = name
                profile_page_toolbar.title = name
                status_text.text = status

                if(profileImage == "no_image") {
                    Glide.with(applicationContext).load(R.drawable.defaultuser).into(profile_page_pimage)
                } else {
                    Glide.with(applicationContext).load(profileImage).into(profile_page_pimage)
                }

            }

        })

    }

    private fun changeImage() {

        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT

        startActivityForResult(Intent.createChooser(intent, "Choose profile image"), GALLERY_REQUEST_CODE)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            dialog = Util.setProgressDialog(this, "Profile image uploading")
            dialog.show()

            try {

                val imageURI = data?.data

                imageURI.let {
                    val imageStream = contentResolver.openInputStream(imageURI!!)
                    val selectedImage = BitmapFactory.decodeStream(imageStream)
                    profile_page_pimage.setImageBitmap(selectedImage)

                    val filePath = mStoreRef
                        .child(Constants.PROFILE_PAGE_FOLDER)
                        .child("${System.currentTimeMillis()}-${mCurrentUser.uid}")

                    filePath.putFile(imageURI).continueWithTask { task ->
                        if(!task.isSuccessful) task.exception?.let { throw it }
                        filePath.downloadUrl
                    }.addOnCompleteListener {
                        if(it.isSuccessful) {
                            val downloadURI = it.result
                            saveImageUrlToStorage(downloadURI.toString())
                        } else {
                            Toast.makeText(this, "Failed", Toast.LENGTH_LONG).show()
                            dialog.dismiss()
                        }
                    }
                }

            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                Toast.makeText(this, "Failed", Toast.LENGTH_LONG).show()

            }
        } else {
            Toast.makeText(this, "Image not selected!", Toast.LENGTH_LONG).show()
        }
    }

    private fun saveImageUrlToStorage(url: String) {
        mDatabase.reference
            .child(Constants.CHILD_USERS)
            .child(mCurrentUser.uid)
            .child(Constants.CHILD_PROFILE_IMAGE)
            .setValue(url).addOnCompleteListener {
                if(it.isComplete) {
                    dialog.dismiss()
                    Toast.makeText(this, "Image Uploaded!", Toast.LENGTH_LONG).show()

                }
            }
    }
}