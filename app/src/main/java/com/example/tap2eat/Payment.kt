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
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.lifecycleScope
import com.example.tap2eat.API.ApiUtilites
import com.example.tap2eat.Utils.PUBLISHIBLE_KEY
import com.google.android.material.button.MaterialButton
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Payment : AppCompatActivity() {

    private lateinit var paymentSheet: PaymentSheet
    private lateinit var customerId: String
    private lateinit var ephemeralKey: String
    private lateinit var clientSecret: String
    lateinit var person:UserDetails

    var amount: Int = 0

    val CHANNEL_ID = "Channel"
    val CHANNEL_NAME = "ChannelName"
    private val apiInterface = ApiUtilites.getApiInterface()

    @SuppressLint("MissingPermission", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_payment)

        createNotificationChannel()
        amount = intent.getIntExtra("EXTRA_TOTAL_AMOUNT", 0)
        val intents= Intent(this,Payment::class.java)
        val pendingIntent= TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(intents)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Pay $amount")
            .setContentText("Pay $amount using UPI to have a hustle free life")
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

        person = (intent.getSerializableExtra("EXTRA_USER_DETAILS") as? UserDetails)!!
        val panel = Profile().apply {
            arguments = Bundle().apply {
                putSerializable("EXTRA_USER_DETAILS", person)
            }
        }

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.topPanel, panel)
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
                } else {
                    Toast.makeText(this@Payment, "Failed to get customer", Toast.LENGTH_SHORT).show()
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
                } else {
                    Toast.makeText(this@Payment, "Failed to get ephemeral key", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this@Payment, "Proceed For Payment", Toast.LENGTH_SHORT).show()
                    paymentFlow()
                } else {
                    val errorMsg = res.errorBody()?.string()
                    Toast.makeText(this@Payment, "Failed to create payment intent: $errorMsg", Toast.LENGTH_LONG).show()
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
                Toast.makeText(this, "Payment Successful", Toast.LENGTH_SHORT).show()
                val successImage = findViewById<ImageView>(R.id.paymentSuccessImage)
                successImage.visibility = View.VISIBLE
                val textView1=findViewById<TextView>(R.id.textView)
                textView1.visibility=View.GONE
                val textView2=findViewById<TextView>(R.id.textView2)
                textView2.visibility=View.GONE
                val button=findViewById<Button>(R.id.contPayment)
                button.visibility=View.GONE

                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                val person = intent.getSerializableExtra("EXTRA_USER_DETAILS") as? UserDetails

                lifecycleScope.launch {
                    delay(3000)
                    val intent = Intent(this@Payment, FoodPage::class.java)
                    intent.putExtra("EXTRA_USER_DETAILS", person)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }


            }
            is PaymentSheetResult.Canceled -> {
                Toast.makeText(this, "Payment Canceled", Toast.LENGTH_SHORT).show()
            }
            is PaymentSheetResult.Failed -> {
                Toast.makeText(this, "Payment Failed: ${paymentSheetResult.error.message}", Toast.LENGTH_LONG).show()
                Log.d("Payment Debug","Payment Failed: ${paymentSheetResult.error.message}")
            }
        }
    }

    fun createNotificationChannel(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            val channel=
                NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
                    lightColor= Color.GREEN
                    enableLights(true)
                }

            val manager=getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
