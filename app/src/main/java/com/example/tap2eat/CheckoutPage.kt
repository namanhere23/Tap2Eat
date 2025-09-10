package com.example.tap2eat

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.tap2eat.API.ApiUtilites
import com.example.tap2eat.Utils.PUBLISHIBLE_KEY
import com.google.android.material.button.MaterialButton
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CheckoutPage : AppCompatActivity() {
    private lateinit var paymentSheet: PaymentSheet
    private lateinit var customerId: String
    private lateinit var ephemeralKey: String
    private lateinit var clientSecret: String
    lateinit var person:UserDetails
    lateinit var cartItems: ArrayList<CartItems>

    var amount: Int = 0

    val CHANNEL_ID = "Channel"
    val CHANNEL_NAME = "ChannelName"
    private val apiInterface = ApiUtilites.getApiInterface()
    @SuppressLint("MissingPermission", "MissingInflatedId")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_checkout_page)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        createNotificationChannel()
        amount = intent.getIntExtra("EXTRA_TOTAL_AMOUNT", 0)
        cartItems = intent.getParcelableArrayListExtra<CartItems>("cartItemsBuy")!!
        val intents = Intent(this, CheckoutPage::class.java)
        val pendingIntent = TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(intents)
            getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Pay $amount")
            .setContentText("Pay $amount using to have a hustle free life")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .build()


        val notificationManager= NotificationManagerCompat.from(this)
        notificationManager.notify(0,notification)

        PaymentConfiguration.init(this, PUBLISHIBLE_KEY)
        paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)

        val button = findViewById<MaterialButton>(R.id.contPayment)
        button.setOnClickListener {
            getCustomerId()
        }
        person= (intent.getSerializableExtra("EXTRA_USER_DETAILS") as? UserDetails)!!

        person?.email?.let {
            loadUserCart(it){  cartItemsSelected ->
                var totalAmount=0
                for (item in cartItemsSelected) {
                    if (item.quantity > 0) {
                        totalAmount += item.price * item.quantity
                    }
                }
                val cartFragment = CartFragment().apply {
                    arguments = Bundle().apply {
                        putParcelableArrayList("EXTRA_ALL_ITEMS", ArrayList(cartItemsSelected))
                        putInt("EXTRA_TOTAL_AMOUNT", totalAmount)
                        putInt("EXTRA_CHECKOUT_OR_NOT", 25)
                    }
                }
                supportFragmentManager.beginTransaction()
                    .replace(R.id.cart, cartFragment)
                    .commit()
            }
        }





        val panel=Profile().apply { arguments= Bundle().apply {
                    putSerializable("EXTRA_USER_DETAILS", person)
            }
        }

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.topPanel,panel)
            commit()
        }
    }

    private fun getCustomerId() {
        lifecycleScope.launch(Dispatchers.IO) {
            val res = apiInterface.getCustomer()
            withContext(Dispatchers.Main) {
                if (res.isSuccessful && res.body() != null) {
                    customerId = res.body()!!.id
                    getEphemeralKey(customerId)
                }
            }
        }
    }

    private fun getEphemeralKey(customerId: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val res = apiInterface.getEphemeralKey(customerId)
            withContext(Dispatchers.Main) {
                if (res.isSuccessful && res.body() != null) {
                    ephemeralKey = res.body()!!.secret
                    getPaymentIntent(customerId)
                }
            }
        }
    }

    private fun getPaymentIntent(customerId: String) {
        val amountInPaise = (amount * 100).toString()
        lifecycleScope.launch(Dispatchers.IO) {
            val res = apiInterface.getPaymentIntent(customerId, amountInPaise)
            withContext(Dispatchers.Main) {
                if (res.isSuccessful && res.body() != null) {
                    clientSecret = res.body()!!.client_secret
                    paymentFlow()
                }
            }
        }
    }

    private fun paymentFlow() {
        paymentSheet.presentWithPaymentIntent(
            clientSecret,
            PaymentSheet.Configuration(
                "Tap2Eat",
                PaymentSheet.CustomerConfiguration(
                    customerId,
                    ephemeralKey
                )
            )
        )
    }

    private fun onPaymentSheetResult(paymentSheetResult: PaymentSheetResult) {
        when (paymentSheetResult) {
            is PaymentSheetResult.Completed -> {
                val successImage = findViewById<ImageView>(R.id.paymentSuccessImage)
                successImage.visibility = View.VISIBLE
                val scroll = findViewById<ScrollView>(R.id.scrollView)
                scroll.visibility = View.GONE



                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK


                val person = intent.getSerializableExtra("EXTRA_USER_DETAILS") as? UserDetails
                person?.let {
                    saveOrder(cartItems, it){ success->
                        if(success)
                            Toast.makeText(this, "Order Placed", Toast.LENGTH_SHORT).show()

                    }
                }

                lifecycleScope.launch {
                    delay(3000)
                    val intent = Intent(this@CheckoutPage, FoodPage::class.java)
                    intent.putExtra("EXTRA_USER_DETAILS", person)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }


            }
            is PaymentSheetResult.Canceled -> {
                Toast.makeText(this, "Payment Canceled", Toast.LENGTH_SHORT).show()
            }

            is PaymentSheetResult.Failed -> TODO()
        }
    }

    fun createNotificationChannel(){
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
            val channel=
                NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
                    lightColor= Color.GREEN
                    enableLights(true)
                }

            val manager=getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun saveOrder(order: ArrayList<CartItems>, user: UserDetails, onResult: (Boolean) -> Unit) {
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }

        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("payments")
        val userId = user.email?.replace(".", "_")
        val orderNames = mutableListOf<String>()
        for (item in order) {
            orderNames.add(item.item)
        }

        if (userId != null) {
            val orderData = mapOf("items" to orderNames, "amount" to amount, "timestamp" to System.currentTimeMillis(), "status" to "Placed")

            val userOrdersRef = usersRef.child(userId)
            userOrdersRef.get().addOnSuccessListener { snapshot ->
                val orderCount = snapshot.childrenCount.toInt()
                userOrdersRef.child("order$orderCount").setValue(orderData)
                    .addOnFailureListener {
                        onResult(false)
                    }
                    .addOnSuccessListener {
                        onResult(true)
                    }
            }
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
}