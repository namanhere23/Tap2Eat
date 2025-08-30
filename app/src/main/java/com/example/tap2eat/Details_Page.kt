package com.example.tap2eat

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File

class Details_Page : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")

    private lateinit var profileImage: ImageView
    private var profileImageUrl: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_details_page)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val mobile=intent.getStringExtra("EXTRA_MOBILE_NUMBER")
        val etName=findViewById<EditText>(R.id.etName)
        profileImage=findViewById<ImageView>(R.id.profileImage)
        val etemail=findViewById<EditText>(R.id.etemail)
        val etpassword=findViewById<EditText>(R.id.etpassword)
        val etMobileDetails=findViewById<EditText>(R.id.etMobileDetails)
        val back=findViewById<ImageView>(R.id.back)

        back.setOnClickListener()
        {
            finish()
        }

        etMobileDetails.setText(mobile)
        val btn=findViewById<com.google.android.material.button.MaterialButton>(R.id.cont2)

        etMobileDetails.setOnClickListener()
        {
            finish()
        }

        btn.setOnClickListener{
            val etName=etName.text.toString()
            val etemail=etemail.text.toString()
            val etpassword=etpassword.text.toString()

            if (etName.isEmpty()) {
                Toast.makeText(this, "Please enter Name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (etemail.isEmpty()) {
                Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (etpassword.isEmpty()) {
                Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (etpassword.length<8) {
                Toast.makeText(this, "Password must be of 8 Characters atleast", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(profileImageUrl.isNullOrEmpty()) {
                Toast.makeText(this, "Upload Profile Picture", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            val userDetails= UserDetails(etName,etemail,etpassword, mobile.toString(),profileImageUrl)
            requestLocationPermission()
            if(!hasLocationPermission()) {
                return@setOnClickListener
            }

            else {
                Intent(this, FoodPage::class.java).also {
                    it.putExtra("EXTRA_USER_DETAILS", userDetails)
                    startActivity(it)
                }
            }

            Toast.makeText(this, "Welcome ${etName} to our App", Toast.LENGTH_LONG).show()
        }

        profileImage.setOnClickListener {
            requestExternalStoragePermission()
            pickImageLauncher.launch("image/*")
        }

    }

    private fun hasExternalStorage() =
        ActivityCompat.checkSelfPermission(this,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED

    private fun requestExternalStoragePermission() {
        if (!hasExternalStorage()) {
            ActivityCompat.requestPermissions(this,arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),0)
        }
    }

    private fun hasLocationPermission() =
        ActivityCompat.checkSelfPermission(this,
            android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

    private fun requestLocationPermission() {
        if (!hasLocationPermission()) {
            ActivityCompat.requestPermissions(this,arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION),0)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            val imageUri = data.data
            profileImage.setImageURI(imageUri)

        }
    }

    private fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val fileName = "user_image_${System.currentTimeMillis()}.jpg"
            val file = File(filesDir, fileName)
            inputStream?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show()
            null
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            profileImage.setImageURI(uri)
            val url=saveImageToInternalStorage(uri)
                profileImageUrl = url
        }
    }
}