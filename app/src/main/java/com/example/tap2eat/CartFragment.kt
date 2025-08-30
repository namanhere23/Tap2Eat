package com.example.tap2eat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tap2eat.adapter.CartAdapter
import com.example.tap2eat.databinding.FragmentCartBinding


class CartFragment : Fragment() {
    private lateinit var binding: FragmentCartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCartBinding.inflate(inflater, container, false)
        val cartItems = arguments?.getParcelableArrayList<CartItems>("EXTRA_ALL_ITEMS") ?: arrayListOf()
        val totalAmount = arguments?.getInt("EXTRA_TOTAL_AMOUNT", 0)
        var sit = arguments?.getInt("EXTRA_CHECKOUT_OR_NOT", 0)
        binding.priceTotal.text = totalAmount.toString()

        val adapter = CartAdapter(cartItems){ total -> binding.priceTotal.text = total.toString() }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter



        binding.button2.setOnClickListener {
            val cartItemsSelected = mutableListOf<CartItems>()
            var totalAmount = 0

            for (item in cartItems) {
                if (item.quantity > 0) {
                    cartItemsSelected.add(item)
                    totalAmount += item.price*item.quantity
                }
            }

            val person = requireActivity()
                .intent
                .getSerializableExtra("EXTRA_USER_DETAILS") as UserDetails

            if(sit==0) {
                val intent = Intent(requireContext(), CheckoutPage::class.java).apply {
                    putExtra("EXTRA_USER_DETAILS", person)
                    putParcelableArrayListExtra("cartItemsBuy", ArrayList(cartItemsSelected))
                    putExtra("EXTRA_TOTAL_AMOUNT", totalAmount)

//                Toast.makeText(requireContext(), "Total Amount: $totalAmount", Toast.LENGTH_SHORT).show()
                }
                startActivity(intent)
            }

            else
            {
                val intent = Intent(requireContext(), Payment::class.java).apply {
                    putExtra("EXTRA_USER_DETAILS", person)
                    putParcelableArrayListExtra("cartItemsBuy", ArrayList(cartItemsSelected))
                    putExtra("EXTRA_TOTAL_AMOUNT", totalAmount)

//                Toast.makeText(requireContext(), "Total Amount: $totalAmount", Toast.LENGTH_SHORT).show()
                }
                startActivity(intent)
            }



        }



        return binding.root
    }

}