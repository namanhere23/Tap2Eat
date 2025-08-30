package com.example.tap2eat

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class FoodPage : AppCompatActivity() {
    lateinit var fusedLocationClient: FusedLocationProviderClient
    @SuppressLint("MissingInflatedId")
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


        if(hasLocationPermission()) {

            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        val latitude = location.latitude.toString()
                        val longitude = location.longitude
                        locationPerson.text = "$latitude, $longitude"

                    } else {
                        Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
                Toast.makeText(this, "Location permission error", Toast.LENGTH_SHORT).show()
            }
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
            android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

    private fun requestLocationPermission() {
        if (!hasLocationPermission()) {
            ActivityCompat.requestPermissions(this,arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION),0)
        }
    }


}