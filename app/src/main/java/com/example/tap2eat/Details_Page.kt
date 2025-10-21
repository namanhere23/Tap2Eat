package com.example.tap2eat

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
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
import android.util.Patterns
import androidx.core.content.ContentProviderCompat.requireContext
import com.bumptech.glide.Glide
import com.example.tap2eat.API.ApiUploadUtilities
import com.example.tap2eat.models.MediaModel
import com.google.android.material.button.MaterialButton
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody

class Details_Page : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")

    private lateinit var profileImage: ImageView
    private var profileImageUrl: String? = null
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_details_page)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val etemail=findViewById<EditText>(R.id.etemail)
        val person = intent.getSerializableExtra("EXTRA_USER_DETAILS") as? UserDetails
        val etName=findViewById<EditText>(R.id.etName)

        profileImage=findViewById<ImageView>(R.id.profileImage)

        val etMobileDetails=findViewById<EditText>(R.id.etMobileDetails)
        val back=findViewById<ImageView>(R.id.back)

        person?.email?.let { email ->
            loadUserByEmail(email) { user ->
                if (user != null) {
                    person.name = user.name
                    person.email = user.email
                    person.mobile = user.mobile
                    etName.setText(user.name ?: "")
                    etemail.setText(user.email ?: "")
                    etMobileDetails.setText(user.mobile ?: "")

                    if (!user.photo.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(user.photo)
                            .into(profileImage)
                        profileImageUrl=user.photo
                    } else {
                        profileImage.setImageResource(R.drawable.ic_profile_pic)
                        profileImageUrl= null
                    }
                }
            }
        }

        back.setOnClickListener()
        {
            finish()
        }

        val history=findViewById<MaterialButton>(R.id.history)
        history.setOnClickListener() {
            Intent(this,History::class.java).also {
                it.putExtra("EXTRA_USER_DETAILS", person)
                startActivity(it)
            }
        }

        if(person!=null)
        {
            etName.setText(person!!.name)
            etemail.setText(person!!.email)
            etMobileDetails.setText(person!!.mobile)
        }

        val btn=findViewById<com.google.android.material.button.MaterialButton>(R.id.cont2)

        etemail.setOnClickListener()
        {
            finish()
        }

        btn.setOnClickListener{

            if(!Patterns.EMAIL_ADDRESS.matcher(etemail.text.toString()).matches()){
                Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            var mobileText = etMobileDetails.text.toString().trim()

            if (mobileText.isEmpty()) {
                Toast.makeText(this, "Please enter mobile number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            mobileText = mobileText.trimStart('0')

            if (mobileText.length != 10 || !mobileText.all { it.isDigit() }) {

                    Toast.makeText(this, "Invalid mobile number", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

            if (etName.text.toString().isEmpty()) {
                Toast.makeText(this, "Please enter Name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userDetails= UserDetails(etName.text.toString(),etemail.text.toString(), etMobileDetails.text.toString(),profileImageUrl)
            saveUser(userDetails) { success ->
                    if (success) {
                        Toast.makeText(this, "User saved!", Toast.LENGTH_SHORT).show()
                    }

                    else {
                        Toast.makeText(this, "Failed to save user!", Toast.LENGTH_SHORT).show()
                    }
                }


            requestLocationPermission()
            if(!hasLocationPermission()) {
                return@setOnClickListener
            }

            else {
                Intent(this, FoodPage::class.java).also {
                    it.putExtra("EXTRA_USER_DETAILS", userDetails)
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(it)
                    finish()
                }
            }

            Toast.makeText(this, "Welcome ${etName.text.toString()} to our App", Toast.LENGTH_LONG).show()
        }

        profileImage.setOnClickListener {
            requestExternalStoragePermission()
            Log.d("Hello","Hello")
            pickImageLauncher.launch("image/*")
            Log.d("Hello","Hello")
        }

        val logout=findViewById<com.google.android.material.button.MaterialButton>(R.id.logout)
        logout.setOnClickListener()
        {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
            }
            val auth = FirebaseAuth.getInstance()
            auth.signOut()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
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
            android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

    private fun requestLocationPermission() {
        if (!hasLocationPermission()) {
            ActivityCompat.requestPermissions(this,arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),0)
        }
    }


    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val url=""
            Log.d("Hello2","Hello2")
            uploadImageToServer(uri){media->
                if(media!=null){
                    profileImageUrl = media.data.url
                }
                else{
                    profileImageUrl = null
                }
            }
            profileImage.setImageURI(uri)

        } else{
            profileImage.setImageResource(R.drawable.ic_profile_pic)
            profileImageUrl = null
        }
    }

    private fun uploadImageToServer(uri: Uri, onResult: (MediaModel?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val context = profileImage.context
            val file = uriToFile(uri, context)

            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("media", file.name, requestFile)

            val response = ApiUploadUtilities.getApiInterface().uploadMedia(body)
            Log.d("Hello4",response.body().toString())

            withContext(Dispatchers.Main) {
            if(response.isSuccessful){
                onResult(response.body())
            } else {
                println("Error in Uploading")
                onResult(null)
            }}
        }
    }

    private fun uriToFile(uri: Uri, context: Context): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val fileName = getFileName(uri, context)
        val tempFile = File(context.cacheDir, fileName)
        tempFile.outputStream().use { output ->
            inputStream?.copyTo(output)
        }
        return tempFile
    }

    private fun getFileName(uri: Uri, context: Context): String {
        var name = "temp_file"
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst()) name = it.getString(nameIndex)
        }
        return name
    }


    private fun saveUser(user: UserDetails , onResult: (Boolean) -> Unit) {
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }

        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("users")
        val userId = user.email?.replace(".", "_")

        if (userId != null) {
            usersRef.child(userId).setValue(user)
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to save user: ${e.message}", Toast.LENGTH_SHORT).show()
                    onResult(false)
                }

                .addOnSuccessListener {
                    onResult(true)
                }
        }

    }

    private fun loadUserByEmail(email: String, onResult: (UserDetails?) -> Unit) {
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }

        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("users")

        val userId = email.replace(".", "_")
        usersRef.child(userId).get()
            .addOnSuccessListener { snap ->
                val user = snap.getValue(UserDetails::class.java)
                onResult(user)
            }
            .addOnFailureListener { e ->
                Log.e("Profile", "Failed to read user", e)
                Toast.makeText(this, "Read failed: ${e.message}", Toast.LENGTH_SHORT).show()
                onResult(null)
            }
    }
}