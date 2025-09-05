package com.example.tap2eat

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.tap2eat.databinding.ActivityPaymentBinding

class Payment : AppCompatActivity() {

    val CHANNEL_ID="Channel"
    val CHANNEL_NAME="ChannelName"
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_payment)
        createNotifiactionChannel()

        val intents= Intent(this,Payment::class.java)
        val pendingIntent= TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(intents)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

        val amount = intent.getIntExtra("EXTRA_TOTAL_AMOUNT", 0)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Pay $amount")
            .setContentText("Pay $amount using UPI to have a hustle free life")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager= NotificationManagerCompat.from(this)
        notificationManager.notify(0,notification)


        if (amount!=0) {
            val qrUrl = "https://quickchart.io/qr?text=upi%3A%2F%2Fpay%3Fpa%3Dnamanhere23%40oksbi%26pn%3DNaman%2520Gulati%26am%3D${amount}.00%26cu%3DINR%26aid%3DuGICAgKCi3anvHw&size=200"
            val qr=findViewById<ImageView>(R.id.qrImage)
            Glide.with(this)
                .load(qrUrl)
                .into(qr)
            qr.setOnClickListener {
                initiateUPIPayment(amount)
            }
        }

        val person=intent.getSerializableExtra("EXTRA_USER_DETAILS") as? UserDetails

        val panel=Profile().apply { arguments= Bundle().apply {
            putSerializable("EXTRA_USER_DETAILS", person)
        }
        }

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.topPanel,panel)
            commit()
        }
    }

    fun createNotifiactionChannel(){
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

    private fun initiateUPIPayment(amount: Int) {
        val upiUri = Uri.parse("upi://pay?pa=namanhere23@oksbi&pn=Naman%20Gulati&am=$amount.00&cu=INR&aid=uGICAgKCi3anvHw")
        val upiIntent = Intent(Intent.ACTION_VIEW, upiUri)

        try {
            startActivity(Intent.createChooser(upiIntent, "Choose UPI App"))
        } catch (e: Exception) {
            Toast.makeText(this, "No UPI app found", Toast.LENGTH_SHORT).show()
        }
    }
}