package com.ntech.chatapp.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.ntech.chatapp.R
import com.ntech.chatapp.util.Constants.CHILD_USERS
import com.ntech.chatapp.util.gone
import com.ntech.chatapp.util.visible
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val mDatabase: FirebaseDatabase by lazy { FirebaseDatabase.getInstance() }
    private lateinit var mReference: DatabaseReference
    private lateinit var mUserReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        btnRegister.setOnClickListener {
            val email = etRegisterEmail.text.toString().trim()
            val username = etRegisterName.text.toString().trim()
            val password = etRegisterPassword.text.toString().trim()

            if(email.isNotEmpty() && password.isNotEmpty() && username.isNotEmpty()) {
                if(password.length >= 6) {
                    pbRegister.visible()
                    register(username, email, password)
                } else {
                    etRegisterPassword.error = "Minimum password length is 6"
                }
            } else {
                if(email.isEmpty()) {
                    etRegisterEmail.error = "Email is empty"
                }

                if(username.isEmpty()) {
                    etRegisterName.error = "Username is empty"
                }

                if(password.isEmpty()) {
                    etRegisterPassword.error = "Password is empty"
                }
            }
        }
    }


    private fun register(name: String, email: String, password: String) {

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {

            if(!it.isSuccessful) {
                Toast.makeText(this, "Failed", Toast.LENGTH_LONG).show()
                pbRegister.gone()
            } else {

                val currentUser = mAuth.currentUser
                val userId = currentUser?.uid

                mReference = mDatabase.reference
                mUserReference = mReference
                    .child(CHILD_USERS)
                    .child(userId!!)

                val userMap = HashMap<String, String>()
                userMap["name"] = name
                userMap["profile_image"] = "no_image"
                userMap["status"] = "Hey there. I am using ChatApp"

                mUserReference.setValue(userMap).addOnCompleteListener {
                    Toast.makeText(this, "Success", Toast.LENGTH_LONG).show()
                    pbRegister.gone()

                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    finish()
                }

            }

        }

    }

}