package com.example.tap2eat

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tap2eat.BuildConfig.API_KEY_LOCATION
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import kotlinx.coroutines.*
import java.io.IOException


class FoodPage : AppCompatActivity() {
    lateinit var fusedLocationClient: FusedLocationProviderClient
    @SuppressLint("MissingInflatedId", "MissingPermission", "RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setContentView(R.layout.activity_food_page)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val cartItems =  arrayListOf(
            CartItems("Pizza", 180, 0, R.drawable.pizza),
            CartItems("Burger", 80, 0, R.drawable.burger),
            CartItems("Sandwich", 50, 0, R.drawable.sandwich),
            CartItems("Tru", 10, 0, R.drawable.tru) ,
            CartItems("Cake", 250, 0, R.drawable.cake),
            CartItems("Dosa", 120, 0, R.drawable.dosa),
            CartItems("Paneer", 150, 0, R.drawable.paneer),
            CartItems("Veg Biryani", 150, 0, R.drawable.vbiryani),
            CartItems("Coffee", 25, 0, R.drawable.coffee)
        )

        val searchView = findViewById<SearchView>(R.id.searchView)
        val searchAutoComplete =
            searchView.findViewById<androidx.appcompat.widget.SearchView.SearchAutoComplete>(
                androidx.appcompat.R.id.search_src_text
            )

        searchAutoComplete.setTextColor(Color.BLACK)       // text color
        searchAutoComplete.setHintTextColor(Color.GRAY)   // hint color

        val cartFragment = CartFragment().apply {
            arguments = Bundle().apply {
                putSerializable("EXTRA_ALL_ITEMS", cartItems)
            }
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.cart, cartFragment)
            .commit()

        requestLocationPermission()
        if(!hasLocationPermission()) {
            Toast.makeText(this, "Give Location Permissions to continue", Toast.LENGTH_SHORT).show()
            finish()
        }

        val locationPerson = findViewById<TextView>(R.id.location)




        if (hasLocationPermission()) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {

                    val latitude = location.latitude
                    val longitude = location.longitude
                    locationPerson.setOnClickListener{
                        Intent(this, Maps::class.java).also {
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
                                Toast.makeText(this@FoodPage, "Failed to get location name", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            requestLocationPermission()
        }



        val person=intent.getSerializableExtra("EXTRA_USER_DETAILS") as UserDetails
        val panel=Profile().apply { arguments= Bundle().apply {
            putSerializable("EXTRA_USER_DETAILS",person)
        } }
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.topPanel,panel)
            commit()
        }

        val offerList = listOf(
            OfferDetails(
                "PDP Offer:- Buy 8 any items and get 2 free",
                0
            ),
            OfferDetails(
                "Buy 3 Grilled Sandwitch and get 1 Amul Tru Free",
                1
            ),
            OfferDetails(
                "Show Student ID and get free Campa on Birthday",
                2
            ))

        val offerContainer = findViewById<LinearLayout>(R.id.offerContainer)

        for(offer in offerList){
            val frame = FrameLayout(this).apply {
                id = View.generateViewId()
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(16, 0, 16, 0)
                }
            }

            offerContainer.addView(frame)

            val fragment = Offers().apply {
                arguments = Bundle().apply {
                    putString("offerText", offer.offerText)
                    putInt("offerCode", offer.backgroundImageOption)
                }
            }

            supportFragmentManager.beginTransaction().apply {
                add(frame.id, fragment)
                commit()
            }

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


}