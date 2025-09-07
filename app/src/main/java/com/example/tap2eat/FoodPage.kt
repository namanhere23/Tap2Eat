package com.example.tap2eat


import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.*


class FoodPage : AppCompatActivity() {
    var cartItems = arrayListOf<CartItems>()
    lateinit var fusedLocationClient: FusedLocationProviderClient
    private var cartFragment: CartFragment? = null

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

        cartFragment = CartFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.cart, cartFragment!!)

            .commit()

        loadItems { success ->
            if (success) {
                cartFragment?.updateCart(cartItems)
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

    private fun loadItems(onResult: (Boolean) -> Unit) {
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }

        val database = FirebaseDatabase.getInstance()
        val itemsRef = database.getReference("items")

        itemsRef.get()
            .addOnSuccessListener { ele ->
                for (itemSnap in ele.children) {
                    val item = itemSnap.getValue(CartItems::class.java)
                    item?.let { cartItems.add(it) }
                }
                onResult(true)
            }

            .addOnFailureListener { e ->
                Log.e("Firebase", "Failed to load items", e)
                onResult(false)
            }

    }

}