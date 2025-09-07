package com.example.tap2eat

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.tap2eat.databinding.ItemOrderBinding
import java.text.SimpleDateFormat
import java.util.*

class OrderAdapter(private val orders: List<Orders>) :
    RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(val binding: ItemOrderBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.binding.tvOrderItems.text = "Items: ${order.items.joinToString(", ")}"
        holder.binding.tvOrderAmount.setTextColor(Color.parseColor("#FF0000"))
        holder.binding.tvOrderAmount.text = "Amount: ₹${order.amount}"
        holder.binding.tvOrderStatus.text = "Status: ${order.status}"
        if(order.status == "Placed"){
            holder.binding.tvOrderStatus.setTextColor(Color.parseColor("#72bf6a"))
        }

        val date = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
            .format(Date(order.timestamp))
        holder.binding.tvOrderDate.text = "Date: $date"
    }

    override fun getItemCount() = orders.size
}
