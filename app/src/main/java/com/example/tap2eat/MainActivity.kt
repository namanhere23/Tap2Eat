package com.example.tap2eat

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val etMobile = findViewById<EditText>(R.id.etMobile)

        var btn=findViewById<MaterialButton>(R.id.cont)

        btn.setOnClickListener {
            var mobileText = etMobile.text.toString().trim()


            if (mobileText.isEmpty()) {
                Toast.makeText(this, "Please enter mobile number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            mobileText = mobileText.trimStart('0')

            if (mobileText.length != 10 || !mobileText.all { it.isDigit() }) {
                Toast.makeText(this, "Invalid mobile number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Welcome To Tap2Eat", Toast.LENGTH_SHORT).show()
            Intent(this, Details_Page::class.java).also {
                it.putExtra("EXTRA_MOBILE_NUMBER", mobileText)
                startActivity(it)
            }
        }
    }
}