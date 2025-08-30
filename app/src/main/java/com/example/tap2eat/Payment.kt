package com.example.tap2eat

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.tap2eat.databinding.ActivityPaymentBinding

class Payment : AppCompatActivity() {
    private lateinit var binding: ActivityPaymentBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_payment)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val amount = intent.getSerializableExtra("EXTRA_TOTAL_AMOUNT").toString()

        if (amount.isNotEmpty()) {
            val qrUrl = "https://quickchart.io/qr?text=upi%3A%2F%2Fpay%3Fpa%3Dnamanhere23%40oksbi%26pn%3DNaman%2520Gulati%26am%3D${amount}.00%26cu%3DINR%26aid%3DuGICAgKCi3anvHw&size=200"

            Glide.with(this)
                .load(qrUrl)
                .into(binding.qrImage)
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
}