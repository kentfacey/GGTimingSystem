package com.ggtimingsystem

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ggtimingsystem.Main.MainActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        login_button_login.setOnClickListener {

            performLogin()


        }

        need_an_account_text_view_login.setOnClickListener {
            Log.d("LoginActivity", "Try to show register activity")

            // Launch login activity
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

    }

    private fun performLogin() {
        val email = email_edittext_login.text.toString()
        val password = password_edittext_login.text.toString()

        if(email.isEmpty() || password.isEmpty()) {
            Toast.makeText( this, "Email or password cannot be empty!", Toast.LENGTH_SHORT).show()
            return
        }


        // Firebase signIn
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener

                // else if successful
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME
                startActivity(intent)
                finish()

            }
            .addOnFailureListener {
                Log.d("Login", "Failed to login: ${it.message}")
                Toast.makeText( this, "Failed to login: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}