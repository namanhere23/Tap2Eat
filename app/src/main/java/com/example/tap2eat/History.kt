package com.example.tap2eat

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase

class History : AppCompatActivity() {
    lateinit var cartItems: ArrayList<CartItems>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_history)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val person = intent.getSerializableExtra("EXTRA_USER_DETAILS") as? UserDetails

        val panel=Profile().apply { arguments= Bundle().apply {
            putSerializable("EXTRA_USER_DETAILS", person)
        }
        }

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.topPanel,panel)
            commit()
        }
        person?.let {
            loadUserOrders(it) { success, orders ->
                if (success && orders != null) {
                    val orderList = orders.map { map ->
                        Orders(
                            items = (map["items"] as? List<String>) ?: emptyList(),
                            amount = (map["amount"] as? Long)?.toInt() ?: 0,
                            timestamp = map["timestamp"] as? Long ?: 0L,
                            status = map["status"] as? String ?: ""
                        )
                    }
                    val reversedList = orderList.reversed()

                    val recyclerView = findViewById<RecyclerView>(R.id.recyclerOrders)
                    recyclerView.layoutManager = LinearLayoutManager(this)
                    recyclerView.adapter = OrderAdapter(reversedList)
                }
            }
        }
    }

    private fun loadUserOrders(user: UserDetails, onResult: (Boolean, List<Map<String, Any>>?) -> Unit) {
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }

        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("payments")
        val userId = user.email?.replace(".", "_")

        if (userId != null) {
            usersRef.child(userId).get()
                .addOnSuccessListener { snap ->
                    if (snap.exists()) {
                        val orders = mutableListOf<Map<String, Any>>()
                        for (orderSnap in snap.children) {
                            val orderData = orderSnap.value as? Map<String, Any>
                            if (orderData != null) {
                                orders.add(orderData)
                            }
                        }
                        onResult(true, orders)
                    } else {
                        onResult(false, null)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Profile", "Failed to read user orders", e)
                    Toast.makeText(this, "Read failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    onResult(false, null)
                }
        } else {
            onResult(false, null)
        }
    }

}