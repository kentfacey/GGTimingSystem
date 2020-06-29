package com.ggtimingsystem

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
import com.ggtimingsystem.model.Run
import com.ggtimingsystem.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import java.time.LocalDateTime
import java.util.*

class RegisterActivity : AppCompatActivity() {
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

    var pictureUri: Uri? = null

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
            }
    }

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

    // test create run in saveUserToFirebaseDatabase
    private fun createRun() {
        val uid = UUID.randomUUID().toString()
        val ref = FirebaseDatabase.getInstance().getReference("runs/$uid")
        val date = LocalDateTime.now().toString()
        val run = Run(uid, date, 5, 100, 150, false)
        ref.setValue(run)
    }

    private fun saveUserToFirebaseDatabase(profileImageUrl: String) {
        createRun()
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(uid, username_edittext_register.text.toString(), profileImageUrl)
        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("RegisterActivity", "Saved user to the Firebase Database")
            }
            .addOnFailureListener {
                // failure
            }
    }
}