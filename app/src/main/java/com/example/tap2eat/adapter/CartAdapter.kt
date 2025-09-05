package com.example.tap2eat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tap2eat.CartItems
import com.example.tap2eat.databinding.CartItemBinding
import java.util.ArrayList

class CartAdapter(private val cartItems: ArrayList<CartItems>, private val onTotalChanged: (Int) -> Unit):
    RecyclerView.Adapter<CartAdapter.CartViewHolder>() {
    private val itemQuantities=IntArray(cartItems.size){1}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding= CartItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return CartViewHolder(binding)
    }

    override fun getItemCount(): Int =cartItems.size

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(cartItems[position])
    }

        inner class CartViewHolder(private val binding: CartItemBinding):RecyclerView.ViewHolder(binding.root) {
            fun bind(item: CartItems) {
                binding.apply {
                    CartQuantity.text = item.quantity.toString()
                    cartItem.text = item.item
                    price.text = item.price.toString()
                    photo.setImageResource(item.photo)
                }

                binding.plusButton.setOnClickListener {
                    item.quantity++
                    binding.CartQuantity.text = item.quantity.toString()
                    onTotalChanged(getTotal())
                }

                binding.minusButton.setOnClickListener {
                    if (item.quantity > 0) {
                        item.quantity--
                        binding.CartQuantity.text = item.quantity.toString()
                        onTotalChanged(getTotal())
                    }
                }
            }
        }

    private fun getTotal(): Int {
        return cartItems.sumOf { it.price * it.quantity }
    }

}