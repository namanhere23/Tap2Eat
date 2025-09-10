package com.example.tap2eat

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.button.MaterialButton
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.io.IOException


class Profile : Fragment() {
    lateinit var fusedLocationClient: FusedLocationProviderClient
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }



        @SuppressLint("MissingPermission")
        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
            val person = arguments?.getSerializable("EXTRA_USER_DETAILS") as? UserDetails
            val profilePic = view.findViewById<ImageView>(R.id.profilePic)
            person?.email?.let { email ->
                loadUserByEmail(email) { user ->
                    if (user != null) {

                        if (!user.photo.isNullOrEmpty()) {
                            Glide.with(this)
                                .load(user.photo)
                                .into(profilePic)
                        } else {
                            profilePic.setImageResource(R.drawable.ic_profile_pic)
                        }
                    }
                }
            }

            profilePic.setOnClickListener {
                Intent(requireContext(), Details_Page::class.java).also {
                    it.putExtra("EXTRA_USER_DETAILS", person)
                    startActivity(it)
                }
            }

            val btnChatbot = view.findViewById<MaterialButton>(R.id.btnChatbot)
            btnChatbot.setOnClickListener {
                val intent = Intent(requireContext(), Gemini::class.java)
                intent.putExtra("EXTRA_USER_DETAILS", person)
                startActivity(intent)
            }

            requestLocationPermission()
            if(!hasLocationPermission()) {
                Toast.makeText(requireContext(), "Give Location Permissions to continue", Toast.LENGTH_SHORT).show()
                requireActivity().finish()
            }

            val locationPerson = view.findViewById<TextView>(R.id.location)

            if (hasLocationPermission()) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {

                        val latitude = location.latitude
                        val longitude = location.longitude
                        locationPerson.setOnClickListener{
                            Intent(requireContext(), Maps::class.java).also {
                                it.putExtra("Lat",latitude.toString())
                                it.putExtra("Long",longitude.toString())
                                startActivity(it)
                            }

                        }

                            CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val client = OkHttpClient()
                                val url = "https://api.openweathermap.org/geo/1.0/reverse?lat=$latitude&lon=$longitude&limit=5&appid=${BuildConfig.API_KEY_LOCATION}"
                                val request = Request.Builder().url(url).build()

                                client.newCall(request).execute().use { response ->
                                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                                    val responseBody = response.body
                                    val body = responseBody?.string()
                                    if (!body.isNullOrEmpty()) {
                                        val jsonArray = JSONArray(body)
                                        val firstObj = jsonArray.getJSONObject(0)
                                        val place = firstObj.getString("name")
                                        val country = firstObj.getString("country")

                                        withContext(Dispatchers.Main) {
                                            locationPerson.text = "$place, $country"
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(requireContext(), "Failed to get location name", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } else {
                        Toast.makeText(requireContext(), "Location not available", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                requestLocationPermission()
            }
        }

    private fun hasLocationPermission() =
        ActivityCompat.checkSelfPermission(requireContext(),
            android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

    private fun requestLocationPermission() {
        if (!hasLocationPermission()) {
            ActivityCompat.requestPermissions(requireContext() as Activity,arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),0)
        }
    }

    private fun loadUserByEmail(email: String, onResult: (UserDetails?) -> Unit) {
        if (FirebaseApp.getApps(requireContext()).isEmpty()) {
            FirebaseApp.initializeApp(requireContext())
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
                Toast.makeText(requireContext(), "Read failed: ${e.message}", Toast.LENGTH_SHORT).show()
                onResult(null)
            }
    }

}