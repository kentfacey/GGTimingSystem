package com.ggtimingsystem

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        need_an_account_text_view.setOnClickListener {
            Log.d("LoginActivity", "Try to show register activity")

            // Launch login activity
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

    }
}