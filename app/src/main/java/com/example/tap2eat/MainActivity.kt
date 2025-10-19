package com.example.tap2eat

import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.os.Handler
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.firebase.functions.FirebaseFunctions
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.android.material.button.MaterialButton
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnContinue: MaterialButton


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        val splashScreen = installSplashScreen()
        var isLoading = true
        splashScreen.setKeepOnScreenCondition { isLoading }
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = Firebase.auth

        val currentUser = auth.currentUser
        if (currentUser != null) {
            var emaill= currentUser.email
            emaill?.let { emaill ->
                loadUserByEmail(emaill) { user ->
                    if (user != null) {
                        if (user.name.toString().isNullOrEmpty()) {
                            val userDetails = UserDetails("", currentUser.email ?: "", "", "")
                            Intent(this, Details_Page::class.java).apply {
                                putExtra("EXTRA_USER_DETAILS", userDetails)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                finish()
                                startActivity(this)
                            }
                        } else {
                            val userDetails = UserDetails("", currentUser.email ?: "", "", "")
                            Intent(this, FoodPage::class.java).apply {
                                putExtra("EXTRA_USER_DETAILS", userDetails)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                finish()
                                startActivity(this)
                            }
                        }
                    }

                    else
                    {
                        val userDetails = UserDetails("", currentUser.email ?: "", "", "")
                        Intent(this, Details_Page::class.java).apply {
                            putExtra("EXTRA_USER_DETAILS", userDetails)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            finish()
                            startActivity(this)
                        }
                    }
                }
            }



        }

        else
        {
            Handler(Looper.getMainLooper()).postDelayed({
                isLoading = false
            }, 2000)
            val kk=findViewById<LinearLayout>(R.id.main)
            kk.visibility= View.VISIBLE
        }


        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnContinue = findViewById(R.id.cont)

        btnContinue.setOnClickListener {
            requestLocationPermission()
            if(!hasLocationPermission()) {
                return@setOnClickListener
            }

            val emailText = etEmail.text.toString().trim()

            if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
                Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (etPassword.text.toString().length < 8) {
                Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            checkIfUserExists(etEmail.text.toString(), etPassword.text.toString())
        }
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)

        tvForgotPassword.setOnClickListener {
            val emailText = etEmail.text.toString().trim()
            if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
                Toast.makeText(this, "Please enter your registered email", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            auth.sendPasswordResetEmail(emailText)
                .addOnSuccessListener {
                    Toast.makeText(
                        this,
                        "Password reset email sent! Check your inbox.",
                        Toast.LENGTH_LONG
                    ).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_LONG).show()}
        }




        val logoGoogle=findViewById<ImageView>(R.id.logo)

        logoGoogle.setOnClickListener {
            requestLocationPermission()
            if(!hasLocationPermission()) {
                return@setOnClickListener
            }

            val googleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId(getString(R.string.default_web_client_id))
                .setFilterByAuthorizedAccounts(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val credentialManager = CredentialManager.create(this)
            lifecycleScope.launch {
                try {
                    val credential = credentialManager.getCredential(this@MainActivity, request)
                    handleSignIn(credential)
                } catch (e: GetCredentialException) {
                    Log.e(TAG, "Google sign-in failed", e)
                    when {
                        e.message?.contains("no credentials available") == true -> {
                            Toast.makeText(
                                this@MainActivity,
                                "No Google accounts found. Please add a Google account to your device.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        e.message?.contains("user_canceled") == true -> {
                        }
                        else -> {
                            Toast.makeText(
                                this@MainActivity,
                                "Google sign-in failed: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }

    }

    private fun checkIfUserExists(email: String, password: String) {
        auth.fetchSignInMethodsForEmail(email)
            .addOnSuccessListener { result ->
                val signInMethods = result.signInMethods

                if (!signInMethods.isNullOrEmpty()) {
                    Toast.makeText(
                        this,
                        "Account found. Enter password to sign in.",
                        Toast.LENGTH_SHORT
                    ).show()
                    signInUser(email, password)
                } else {
                    createNewUser(email,password)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to check email: ${e.message}", Toast.LENGTH_LONG)
                    .show()
                Log.d("LoginDebug", "Failed to check email: ${e.message}")
            }
    }

    private fun signInUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user != null) {
                    if (user.isEmailVerified) {
                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Please verify your email before logging in.", Toast.LENGTH_LONG).show()
                        auth.signOut()
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun createNewUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                result.user?.sendEmailVerification()
                    ?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Verification email sent! Check your inbox.", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this, "Failed to send verification email: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
            .addOnFailureListener { e ->
                if (e.message?.contains("already in use") == true) {
                    signInUser(email, password)
                } else {
                    Toast.makeText(this, "Registration failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun goToDetails(email: String) {
        val userDetails = UserDetails("", email, "", "")
        startActivity(Intent(this, Details_Page::class.java).apply {
            putExtra("EXTRA_USER_DETAILS", userDetails)
        })
        finish()
    }

    private fun handleSignIn(response: GetCredentialResponse) {
        val credential = response.credential
        if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
        } else {
            Log.w(TAG, "Credential is not of type Google ID!")
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val auth = FirebaseAuth.getInstance()
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    val name = user?.displayName
                    val email = user?.email
                    val phone = user?.phoneNumber
                    val photoUrl = user?.photoUrl?.toString()
                    var finalname= name
                    var finalEmail= email
                    var finalMobile=phone
                    var finalphoto=photoUrl

                    email?.let { email ->
                        loadUserByEmail(email) { userr ->
                            if (userr != null) {
                                finalname = (userr.name ?: name).toString()
                                finalEmail = userr.email ?: email
                                finalMobile = userr?.mobile ?: phone
                                finalphoto = (userr.photo ?: photoUrl).toString()
                            }

                            val profile=UserDetails(finalname, finalEmail, finalMobile, finalphoto)

                            saveUser(profile) { success ->
                                var emaill= user?.email
                                emaill?.let { emaill ->
                                    loadUserByEmail(emaill) { user ->
                                        if (user != null) {
                                            if (finalMobile?.length !=10) {
                                                val userDetails = UserDetails( name ?: "",  email ?: "", phone ?: "",photoUrl ?: "" )
                                                Intent(this, Details_Page::class.java).apply {
                                                    putExtra("EXTRA_USER_DETAILS", userDetails)
                                                    startActivity(this)
                                                }
                                            } else {
                                                val userDetails = UserDetails( name ?: "",  email ?: "", phone ?: "",photoUrl ?: "" )
                                                Intent(this, FoodPage::class.java).apply {
                                                    putExtra("EXTRA_USER_DETAILS", userDetails)
                                                    startActivity(this)
                                                }
                                            }
                                        }

                                        else
                                        {
                                            val userDetails = UserDetails( name ?: "",  email ?: "", phone ?: "",photoUrl ?: "" )
                                            Intent(this, Details_Page::class.java).apply {
                                                putExtra("EXTRA_USER_DETAILS", userDetails)
                                                startActivity(this)
                                            }
                                        }
                                    }
                                }
                            }

                        }
                    }



                    Toast.makeText(this, "Google sign-in successful!", Toast.LENGTH_SHORT).show()



                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(
                        this,
                        "Firebase authentication failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun sendPreRegistrationEmail(email: String, onResult: (Boolean) -> Unit) {
        val functions = FirebaseFunctions.getInstance()
        functions.getHttpsCallable("sendPreRegistrationEmail")
            .call(mapOf("email" to email))
            .addOnSuccessListener {
                Toast.makeText(this, "Confirmation email sent!", Toast.LENGTH_SHORT).show()
                onResult(true)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to send email: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.d("LoginDebugIssue", "Failed to send email: ${e.message}")
                Log.d("LoginDebugIssue", "Failed to send email: ${email}")
                onResult(false)
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
        if (FirebaseApp.getApps(this    ).isEmpty()) {
            FirebaseApp.initializeApp(this  )
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
                Toast.makeText(this , "Read failed: ${e.message}", Toast.LENGTH_SHORT).show()
                onResult(null)
            }
    }
}