package com.example.tap2eat

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class CheckoutPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_checkout_page)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        var sit=intent.getIntExtra("EXTRA_CHECKOUT_OR_NOT",0)

        val cartItems = intent.getParcelableArrayListExtra<CartItems>("cartItemsBuy") ?: arrayListOf()
        val totalAmount=intent.getIntExtra("EXTRA_TOTAL_AMOUNT",0)
        val cartFragment = CartFragment().apply {
            arguments = Bundle().apply {
                putParcelableArrayList("EXTRA_ALL_ITEMS", cartItems)
                putInt("EXTRA_TOTAL_AMOUNT", totalAmount)
                sit=sit+1
                putInt("EXTRA_CHECKOUT_OR_NOT", sit)
            }
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.cart, cartFragment)
            .commit()

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