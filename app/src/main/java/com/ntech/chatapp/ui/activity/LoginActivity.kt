package com.ntech.chatapp.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.ntech.chatapp.R
import com.ntech.chatapp.util.gone
import com.ntech.chatapp.util.visible
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private val mAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        login_button.setOnClickListener {
            val email = login_email.text.toString().trim()
            val password = login_password.text.toString().trim()

            if(email.isNotEmpty() && password.isNotEmpty()) {
                login_progressBar.visible()
                login(email, password)
            } else {

                if(email.isEmpty())
                    login_email.error = "Email is empty"

                if(password.isEmpty())
                    login_email.error = "Password is empty"

            }
        }

        login_register_button.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun login(email: String, password: String) {

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if(!it.isSuccessful) {
                Toast.makeText(this, "Failed", Toast.LENGTH_LONG).show()
                login_progressBar.gone()
            }else {
                login_progressBar.visible()
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }

    }
}