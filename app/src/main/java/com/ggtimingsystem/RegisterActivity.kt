package com.ggtimingsystem

import android.app.Activity
import android.content.Intent
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        register_button_register.setOnClickListener {
            performRegister()
        }
        already_have_account_text_view_register.setOnClickListener {
            Log.d("RegisterActivity", "Try to show login activity")

            // Go back to login activity
            finish()
        }
        picture_button_register.setOnClickListener {
            Log.d("RegisterActivity", "Try to show photo selector")

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }
    }

    var pictureUri: Uri? = null

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            // photo has been selected
            Log.d("Register", "Photo was selected")
            pictureUri = data.data

            //select photo from uri
            val bitmap = when {
                Build.VERSION.SDK_INT < 28 -> MediaStore.Images.Media.getBitmap(
                    this.contentResolver,
                    pictureUri
                )
                else -> {
                    val source = ImageDecoder.createSource(this.contentResolver, pictureUri!!)
                    ImageDecoder.decodeBitmap(source)
                }
            }

            val bitmapDrawable = BitmapDrawable(bitmap)
            picture_button_register.setBackgroundDrawable(bitmapDrawable)
        }
    }

    private fun performRegister() {
        val email = email_edittext_register.text.toString()
        val password = password_edittext_register.text.toString()

        if(email.isEmpty() || password.isEmpty()) {
            Toast.makeText( this, "Email or password cannot be empty!", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("RegisterActivity", "Email is: $email")
        Log.d("RegisterActivity", "Password:$password")

        //Create user with Firebase Authentication
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener

                // else if successful
                Log.d("Register", "Created user with uid: ${it.result?.user?.uid}")
                Toast.makeText( this, "Create user with email: $email", Toast.LENGTH_SHORT).show()
                uploadImageToFirebaseSorage()

            }
            .addOnFailureListener {
                Log.d("Register", "Failed to create user: ${it.message}")
                Toast.makeText( this, "Failed to create user: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadImageToFirebaseSorage() {

    }
}