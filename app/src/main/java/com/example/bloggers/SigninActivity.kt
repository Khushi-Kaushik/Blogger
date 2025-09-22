package com.example.bloggers

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.bloggers.databinding.ActivitySigninBinding
import com.example.bloggers.model.userData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import io.appwrite.Client
import io.appwrite.ID
import io.appwrite.models.InputFile
import io.appwrite.services.Storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import android.util.Log
import com.example.bloggers.register.StartActivity


class SigninActivity : AppCompatActivity() {

    private val TAG = "SigninActivity"

    private val binding: ActivitySigninBinding by lazy {
        ActivitySigninBinding.inflate(layoutInflater)
    }
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: Storage

    private var selectedImageUri: Uri? = null
    private val IMAGE_PICK_CODE = 1000



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        Log.d(TAG, "onCreate started")

        // Firebase init
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://bloggers-96929-default-rtdb.asia-southeast1.firebasedatabase.app/")

        // Appwrite init
        val client = Client(this)
            .setEndpoint("https://nyc.cloud.appwrite.io/v1")
            .setProject("689cf4f9002b26652617")
        storage = Storage(client)

        val action = intent.getStringExtra("action")
        Log.d(TAG, "Action: $action")

        if (action == "login") {
            Log.d(TAG, "Showing login UI")
            // Show login UI
            binding.email.visibility = View.VISIBLE
            binding.password.visibility = View.VISIBLE
            binding.loginRegistered.visibility = View.VISIBLE
            binding.buttonRegistered.isEnabled = false
            binding.buttonRegistered.alpha = 0.5f
            binding.textRegistered.isEnabled = false
            binding.textRegistered.alpha = 0.5f
            binding.nameRegistered.visibility = View.GONE
            binding.passwordRegistered.visibility = View.GONE
            binding.emailRegistered.visibility = View.GONE
            binding.cardView.visibility = View.GONE

            binding.loginRegistered.setOnClickListener {
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()
            if(email.isEmpty() || password.isEmpty()){
                Toast.makeText(
                    this,
                    "Please fill all the details",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()

                            // Save session
                            val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
                            sharedPref.edit().putBoolean("isLoggedIn", true).apply()

                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                        else {
                            Toast.makeText(
                                this,
                                "Please enter correct details",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
            }


        } else if (action == "register") {
            Log.d(TAG, "Showing registration UI")
            binding.loginRegistered.isEnabled = false
            binding.loginRegistered.alpha = 0.5f

            binding.buttonRegistered.setOnClickListener {
                val registeredName = binding.nameRegistered.text.toString()
                val registeredEmail = binding.emailRegistered.text.toString()
                val registeredPassword = binding.passwordRegistered.text.toString()

                Log.d(TAG, "Register button clicked")
                Log.d(TAG, "Name: $registeredName, Email: $registeredEmail")

                if (registeredName.isEmpty() || registeredEmail.isEmpty() || registeredPassword.isEmpty() || selectedImageUri == null) {
                    Toast.makeText(
                        this,
                        "Please enter all details and select an image",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d(TAG, "Missing input or image")
                } else {
                    Log.d(TAG, "Creating user with email: $registeredEmail")
                    auth.createUserWithEmailAndPassword(registeredEmail, registeredPassword)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d(TAG, "Firebase registration successful")
                                val user = auth.currentUser
                                auth.signOut()
                                user?.let {
                                    val userId = user.uid
                                    val userReference = database.getReference("users")
                                    val file = File(getRealPathFromUri(selectedImageUri!!))
                                    val bucketId = "689cf52800389eb02094"

                                    Log.d(TAG, "Starting image upload")
                                    CoroutineScope(Dispatchers.IO).launch {
                                        try {
                                            val uploadedFile = storage.createFile(
                                                bucketId,
                                                ID.unique(),
                                                InputFile.fromFile(file),
                                                permissions = listOf("read(\"any\")")
                                            )

                                            val fileId = uploadedFile.id
                                            val fileUrl =
                                                "https://nyc.cloud.appwrite.io/v1/storage/buckets/$bucketId/files/$fileId/view?project=689cf4f9002b26652617"

                                            Log.d(TAG, "File uploaded, fileUrl: $fileUrl")

                                            withContext(Dispatchers.Main) {
                                                val newUserData = userData(
                                                    registeredName,
                                                    registeredEmail,
                                                    profileImageUrl = fileUrl
                                                )
                                                userReference.child(userId).setValue(newUserData)
                                                Toast.makeText(this@SigninActivity, "Registration successful", Toast.LENGTH_SHORT).show()

                                                // Save session
                                                val sharedPref = getSharedPreferences("UserSession", MODE_PRIVATE)
                                                sharedPref.edit().putBoolean("isLoggedIn", true).apply()

                                                startActivity(Intent(this@SigninActivity, MainActivity::class.java))
                                                finish()
                                            }


                                        } catch (e: Exception) {
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(
                                                    this@SigninActivity,
                                                    "Upload failed: ${e.message}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                Log.e(TAG, "Upload failed", e)
                                            }
                                        }
                                    }
                                }
                            } else {
                                Toast.makeText(
                                    this,
                                    "Registration failed: ${task.exception?.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.e(TAG, "Firebase registration failed", task.exception)
                            }
                        }
                }
            }



            binding.cardView.setOnClickListener {
                Log.d(TAG, "Select image clicked")
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(
                    Intent.createChooser(intent, "Select Image"),
                    IMAGE_PICK_CODE
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "onActivityResult: requestCode=$requestCode, resultCode=$resultCode")
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE && data != null && data.data != null) {
            selectedImageUri = data.data
            Log.d(TAG, "Image selected: $selectedImageUri")
            binding.userImage.setImageURI(selectedImageUri)
        }
    }

    private fun getRealPathFromUri(uri: Uri): String {
        val projection = arrayOf(android.provider.MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        cursor?.moveToFirst()
        val columnIndex = cursor?.getColumnIndexOrThrow(android.provider.MediaStore.Images.Media.DATA)
        val filePath = cursor?.getString(columnIndex!!)
        cursor?.close()
        Log.d(TAG, "getRealPathFromUri: $filePath")
        return filePath ?: ""
    }
}
