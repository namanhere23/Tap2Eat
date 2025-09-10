package com.example.tap2eat


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class FoodPage : AppCompatActivity() {
    var cartItems = arrayListOf<CartItems>()
    lateinit var fusedLocationClient: FusedLocationProviderClient
    private var cartFragment: CartFragment? = null

    private var drawerLayout: DrawerLayout? = null
    private var navigationView: NavigationView? = null
    private var toolbar: MaterialToolbar? = null

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

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)

        val person = intent.getSerializableExtra("EXTRA_USER_DETAILS") as UserDetails

        navigationView?.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    Intent(this,FoodPage::class.java).also {
                        it.putExtra("EXTRA_USER_DETAILS", person)
                        startActivity(it)
                    }
                }
                R.id.nav_orders -> {
                    Intent(this,History::class.java).also {
                        it.putExtra("EXTRA_USER_DETAILS", person)
                        startActivity(it)
                    }
                }
                R.id.nav_logout -> {
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
            drawerLayout?.closeDrawers()
            true
        }

        cartFragment = CartFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.cart, cartFragment!!)

            .commit()


        val k: MutableMap<String, Int> = mutableMapOf()
        loadItems { success ->
            if (success) {
                var cartItemsProceed = arrayListOf<CartItems>()
                person.email?.let {
                    loadUserCart(it) { cartItemsPre ->
                        for (items in cartItemsPre) {
                            k[items.item] = items.quantity
                        }
                        for (items in cartItems) {
                            val quantity = k[items.item] ?: 0
                            if (quantity > 0) {
                                cartItemsProceed.add(
                                    CartItems(
                                        item = items.item,
                                        price = items.price,
                                        quantity = quantity,
                                        photo = items.photo
                                    )
                                )
                            } else {
                                cartItemsProceed.add(
                                    CartItems(
                                        item = items.item,
                                        price = items.price,
                                        0,
                                        photo = items.photo
                                    )
                                )
                            }
                        }
                        cartFragment?.updateCart(cartItemsProceed)
                    }

                }

            }
        }

        val headerView = navigationView?.getHeaderView(0)
        val headerProfilePic = headerView?.findViewById<ImageView>(R.id.headerProfilePic)
        val headerUserName = headerView?.findViewById<TextView>(R.id.headerUserName)


        if (person != null && !person.email.isNullOrEmpty()) {
            loadUserByEmail(person.email!!) { user ->
                if (user != null) {
                    headerUserName?.text = user.name ?: "Welcome User"

                    if (!user.photo.isNullOrEmpty()) {
                        if (headerProfilePic != null) {
                            Glide.with(this)
                                .load(user.photo)
                                .into(headerProfilePic)
                        }
                    } else {
                        headerProfilePic?.setImageResource(R.drawable.ic_profile_pic)
                    }
                }
            }
        }


        val panel = Profile().apply {
            arguments = Bundle().apply {
                putSerializable("EXTRA_USER_DETAILS", person)
            }
        }
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.topPanel, panel)
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
            )
        )

        val offerContainer = findViewById<LinearLayout>(R.id.offerContainer)

        for (offer in offerList) {
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

    private fun loadUserCart(email: String, onResult: (List<CartItems>) -> Unit) {
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }

        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("cart")

        val userId = email.replace(".", "_")
        usersRef.child(userId)
            .get()
            .addOnSuccessListener { snap ->
                val cartList = mutableListOf<CartItems>()
                for (itemSnap in snap.children) {
                    val item = itemSnap.getValue(CartItems::class.java)
                    item?.let { cartList.add(it) }
                }
                onResult(cartList)
            }
            .addOnFailureListener { e ->
                onResult(emptyList())
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