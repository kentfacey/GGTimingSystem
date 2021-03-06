package com.ggtimingsystem.authentication

import android.app.Activity
import android.content.Intent
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.ggtimingsystem.R
import com.ggtimingsystem.database.Database
import com.ggtimingsystem.main.MainActivity
import com.ggtimingsystem.models.Run
import com.ggtimingsystem.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

class RegisterActivity : AppCompatActivity() {

    private var pictureUri: Uri? = null

    private val database = Database()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        register_button_register.setOnClickListener {
            performRegister()
        }
        already_have_account_text_view_register.setOnClickListener {
            Log.d("RegisterActivity", "Try to show login activity")

            // Launch login activity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        picture_button_register.setOnClickListener {
            Log.d("RegisterActivity", "Try to show photo selector")

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            // photo has been selected
            Log.d("RegisterActivity", "Photo was selected")
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

            picture_image_view_register.setImageBitmap(bitmap)

            picture_button_register.alpha = 0f
        }
    }

    private fun performRegister() {
        val email = email_edittext_register.text.toString()
        val password = password_edittext_register.text.toString()
        val username = username_edittext_register.text.toString()

        if(email.isEmpty() || password.isEmpty() || username.isEmpty()) {
            Toast.makeText( this, "Email or password cannot be empty!", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("RegisterActivity", "Email is: $email")
        Log.d("RegisterActivity", "Password:$password")

        var isSuccessful = true

        //Create user with Firebase Authentication
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener

                // else if successful
                Log.d("RegisterActivity", "Created user with uid: ${it.result?.user?.uid}")
                Toast.makeText( this, "Create user with email: $email", Toast.LENGTH_SHORT).show()

                if(pictureUri != null) {
                    uploadImageToFirebaseStorage()
                }
                else {
                    saveUserToFirebaseDatabase(pictureUri.toString())
                }


            }
            .addOnFailureListener {
                Log.d("RegisterActivity", "Failed to create user: ${it.message}")
                Toast.makeText( this, "Failed to create user: ${it.message}", Toast.LENGTH_SHORT).show()
                isSuccessful = false
            }

        if(isSuccessful) {
            // Firebase signIn
            signInWithEmailAndPassword(email, password)
        }
    }


    // Firebase signIn
    private fun signInWithEmailAndPassword(email: String, password: String){
        // Firebase signIn
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener

                // else if successful
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()

            }
            .addOnFailureListener {
                Log.d("Login", "Failed to login: ${it.message}")
                Toast.makeText( this, "Failed to login: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // uploads an image to the firebase storage
    private fun uploadImageToFirebaseStorage() {
        if (pictureUri == null) return
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(pictureUri!!)
            .addOnSuccessListener {
                Log.d("RegisterActivity", "Uploaded image: ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener {
                    Log.d("RegisterActivity", "File Location: $it")

                    saveUserToFirebaseDatabase(it.toString())
                }
            }
            .addOnFailureListener {
                // failure
            }
    }


    private fun saveUserToFirebaseDatabase(profileImageUrl: String) {

        // calls the test create run method when saving a user to the database
        database.createRun()

        val uid = FirebaseAuth.getInstance().uid ?: ""
        val username = username_edittext_register.text.toString()

        database.saveUser(uid, profileImageUrl, username)

    }
}