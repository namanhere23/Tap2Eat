package com.example.tap2eat

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tap2eat.adapter.CartAdapter
import com.example.tap2eat.databinding.FragmentCartBinding
import retrofit2.http.Query
import java.util.Locale


class CartFragment : Fragment() {
    private lateinit var binding: FragmentCartBinding
    private lateinit var adapter: CartAdapter
    lateinit var person:UserDetails
    private lateinit var cartItemsSelected: MutableList<CartItems>
    var filteredList=ArrayList<CartItems>()
    private var cartItems: ArrayList<CartItems> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCartBinding.inflate(inflater, container, false)
        val sit = arguments?.getInt("EXTRA_CHECKOUT_OR_NOT", 0) ?: 0
        val totalAmount = arguments?.getInt("EXTRA_TOTAL_AMOUNT") ?: 0
        binding.priceTotal.text = totalAmount.toString()


        cartItems=arguments?.getParcelableArrayList<CartItems>("EXTRA_ALL_ITEMS") ?: arrayListOf()

        adapter = CartAdapter { total -> binding.priceTotal.text = total.toString() }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter


        adapter.updateList(cartItems)

        binding.searchView.clearFocus();
        styleSearchView()

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true
            }

        })

//        Toast.makeText(requireContext(), "Activity ${sit}", Toast.LENGTH_SHORT).show()


        binding.button2.setOnClickListener {
            cartItemsSelected = mutableListOf<CartItems>()
            var totalAmount = 0

            for (item in cartItems) {
                if (item.quantity > 0) {
                    cartItemsSelected.add(item)
                    totalAmount += item.price * item.quantity
                }
            }

            person = requireActivity()
                .intent
                .getSerializableExtra("EXTRA_USER_DETAILS") as UserDetails


            if (sit == 0) {
                if(totalAmount==0)
                {
                    Toast.makeText(requireContext(),"Add something!!",Toast.LENGTH_SHORT).show()
                }

                else {
                    val intent = Intent(requireContext(), CheckoutPage::class.java).apply {
                        putExtra("EXTRA_USER_DETAILS", person)
                        putParcelableArrayListExtra("cartItemsBuy", ArrayList(cartItemsSelected))
                        putExtra("EXTRA_TOTAL_AMOUNT", totalAmount)

//                Toast.makeText(requireContext(), "Total Amount: $totalAmount", Toast.LENGTH_SHORT).show()
                    }
                    startActivity(intent)
                }
            }

        }

        if(sit!=0)
        {
            binding.button2.visibility = View.GONE

        }

        return binding.root
    }

    private fun filterList(query: String?) {
        if (query.isNullOrEmpty()) {
            adapter.updateList(cartItems)
            return
        }

        val filteredList = cartItems.filter {
            it.item.lowercase(Locale.ROOT).contains(query.lowercase(Locale.ROOT))
        }

        if (filteredList.isEmpty()) {
            Toast.makeText(requireContext(), "No Data", Toast.LENGTH_LONG).show()
        }

        adapter.updateList(filteredList)
    }

    fun updateCart(newList: List<CartItems>) {
        cartItems.clear()
        cartItems.addAll(newList)
        adapter.updateList(cartItems)
    }

    @SuppressLint("RestrictedApi")
    private fun styleSearchView() {
        val searchEditText = binding.searchView.findViewById<androidx.appcompat.widget.SearchView.SearchAutoComplete>(
            androidx.appcompat.R.id.search_src_text
        )
        searchEditText.setTextColor(Color.BLACK)
        searchEditText.setHintTextColor(Color.GRAY)
    }



}