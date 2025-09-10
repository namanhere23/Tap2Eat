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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.tap2eat.adapter.CartAdapter
import com.example.tap2eat.databinding.FragmentCartBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import java.util.Locale


class CartFragment : Fragment() {
    private lateinit var binding: FragmentCartBinding
    private lateinit var adapter: CartAdapter
    lateinit var person: UserDetails
    private lateinit var cartItemsSelected: MutableList<CartItems>
    var filteredList = ArrayList<CartItems>()
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


        cartItems = arguments?.getParcelableArrayList<CartItems>("EXTRA_ALL_ITEMS") ?: arrayListOf()

        adapter = CartAdapter { total -> binding.priceTotal.text = total.toString() }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter


        adapter.updateList(cartItems)

        binding.searchView.clearFocus();
        styleSearchView()

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true
            }

        })


        person = requireActivity()
            .intent
            .getSerializableExtra("EXTRA_USER_DETAILS") as UserDetails

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

            saveUserCart(person, ArrayList(cartItemsSelected), totalAmount) { success ->
                if (success) {
                    if (sit == 0) {
                        if (totalAmount == 0) {
                            Toast.makeText(requireContext(), "Add something!!", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            val intent = Intent(requireContext(), CheckoutPage::class.java).apply {
                                putExtra("EXTRA_USER_DETAILS", person)
                                putParcelableArrayListExtra("cartItemsBuy", ArrayList(cartItemsSelected))
                                putExtra("EXTRA_TOTAL_AMOUNT", totalAmount)

                            }
                            startActivity(intent)
                        }
                    }
                }
            }

        }

        binding.deleteCart.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to clear your cart?")
                .setPositiveButton("Yes") { dialog, whichButton ->
                    person.email?.let { it1 ->
                        clearUserCart(it1) { success ->
                            if (success) {
                                val currentActivity = requireActivity()
                                if (currentActivity is CheckoutPage) {
                                    val intent = Intent(currentActivity, FoodPage::class.java)
                                    intent.putExtra("EXTRA_USER_DETAILS", person)
                                    currentActivity.startActivity(intent)
                                    currentActivity.finish()
                                } else if (currentActivity is FoodPage) {
                                    val intent = requireActivity().intent
                                    requireActivity().finish()
                                    requireActivity().startActivity(intent)
                                }
                            }
                        }
                    }
                }

                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()


        }


        if (sit != 0) {
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
        updateTotal()
    }

    private fun updateTotal() {
        val total = cartItems.sumOf { it.price * it.quantity }
        binding.priceTotal.text = total.toString()
    }

    @SuppressLint("RestrictedApi")
    private fun styleSearchView() {
        val searchEditText =
            binding.searchView.findViewById<androidx.appcompat.widget.SearchView.SearchAutoComplete>(
                androidx.appcompat.R.id.search_src_text
            )
        searchEditText.setTextColor(Color.BLACK)
        searchEditText.setHintTextColor(Color.GRAY)
    }

    private fun saveUserCart(user: UserDetails, cartItems: ArrayList<CartItems>, amount: Int, onResult: (Boolean) -> Unit
    ) {
        if (FirebaseApp.getApps(requireContext()).isEmpty()) {
            FirebaseApp.initializeApp(requireContext())
        }

        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("cart")
        val userId = user.email?.replace(".", "_")

        if (userId != null) {
            val userCartRef = usersRef.child(userId)

            val cartData = cartItems.associateBy(
                { it.item },
                {
                    mapOf("item" to it.item, "price" to it.price, "quantity" to it.quantity, "photo" to it.photo)
                }
            )

            userCartRef.setValue(cartData)
                .addOnSuccessListener { onResult(true) }
                .addOnFailureListener { onResult(false) }
        } else {
            onResult(false)
        }
    }

    fun clearUserCart(email: String, onResult: (Boolean) -> Unit) {
        if (FirebaseApp.getApps(requireContext()).isEmpty()) {
            FirebaseApp.initializeApp(requireContext())
        }

        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("cart")
        val userId = email.replace(".", "_")
        if (userId.isNotEmpty()) {
            val userCartRef = usersRef.child(userId)
            userCartRef.removeValue()
                .addOnSuccessListener {
                    onResult(true)
                }
                .addOnFailureListener { e ->
                    onResult(false)
                }
        } else {
            onResult(false)
        }
    }


}
