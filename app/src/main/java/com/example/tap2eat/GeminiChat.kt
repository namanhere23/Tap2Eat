package com.example.tap2eat

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.example.tap2eat.MessageModel
import com.example.tap2eat.BuildConfig
import com.example.tap2eat.CartItems
import com.example.tap2eat.Orders
import com.example.tap2eat.UserDetails
import com.google.ai.client.generativeai.type.Content
import android.widget.Toast
import androidx.annotation.RequiresApi
import android.os.Build

class ChatViewModel : ViewModel() {

    val cartItems = arrayListOf<CartItems>()
    val orders = arrayListOf<Orders>()
    val messageList = mutableStateListOf<MessageModel>()
    val ordersLoaded = mutableStateOf(false)

    private val gson = Gson()
    private var chat: com.google.ai.client.generativeai.Chat? = null

    private val systemPrompt = """
        You are Tap2Eat AI assistant.  
        Always reply in plain, natural conversational text — like you are a friendly waiter or food guide.  

        Rules:
        - Do not use Bold text anywhere  
        - Use ONLY the food items that exist in the given list {cartJson}.  
        - Never invent or suggest items outside this list.  
        - Always mention the name, price (₹ INR), and give a short, tasty description.  
        - Keep the tone warm, fun, and professional, like you are recommending dishes to a friend.  
        - Do not reply in JSON or code format. Only use normal human-style text.
        - Also giving you the past order history of the customer please use that also for ChatBot  

        Restrictions:
        - Only Indian-legal food items allowed.  
        - Never mention beef or beef-based dishes.  
        - Redirect off-topic queries back to food context.  
        - Mention quantities if user specifies, otherwise assume 1.  
    """.trimIndent()

    private val systemPrompt2 = """
        You have the following past orders of the user: {orderJson}.
        Use this info while answering questions about their order history.
    """

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.API_KEY_GEMINI
    )

    fun sendMessage(question: String) {
        if (!ordersLoaded.value) return
        messageList.add(MessageModel(question, "user"))

        viewModelScope.launch {
            try {
                messageList.add(MessageModel("Typing...", "model"))
                val cartJson = gson.toJson(cartItems)

                val orderJson = gson.toJson(orders)
                val fullPrompt = systemPrompt
                    .replace("{cartJson}", cartJson) +
                        "\n" + systemPrompt2.replace("{orderJson}", orderJson)

                val chat = generativeModel.startChat(
                    history = buildList {
                        add(content(role = "user") { text(fullPrompt) })
                        add(content(role = "model") { text("Understood. I will only answer about food.") })
                        addAll(messageList.map {
                            content(role = it.role) { text(it.message) }
                        })
                    }
                )

                val response = chat.sendMessage(question)

                messageList.removeLast()
                messageList.add(MessageModel(response.text.toString(), "model"))

            } catch (e: Exception) {
                messageList.removeLast()
                messageList.add(MessageModel("Error: ${e.message}", "model"))
            }
        }

    }

    fun loadItems(context: Context, onResult: (Boolean) -> Unit) {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }

        val database = FirebaseDatabase.getInstance()
        val itemsRef = database.getReference("items")

        itemsRef.get()
            .addOnSuccessListener { ele ->
                for (itemSnap in ele.children) {
                    val item = itemSnap.getValue(CartItems::class.java)
                    item?.let { cartItems.add(it) }
                }
                Log.d("FirebaseItems", "Fetched items: ${cartItems.size}")
                onResult(true)
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Failed to load items", e)
                onResult(false)
            }
    }

    fun loadUserOrders(context: Context, user: UserDetails, onResult: (Boolean, List<Orders>?) -> Unit) {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }

        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("payments")
        val userId = user.email?.replace(".", "_")

        if (userId != null) {
            usersRef.child(userId).get()
                .addOnSuccessListener { snap ->
                    if (snap.exists()) {
                        val fetchedOrders = mutableListOf<Orders>()
                        for (orderSnap in snap.children) {
                            val orderData = orderSnap.getValue(Orders::class.java)
                            orderData?.let { fetchedOrders.add(it) }
                        }
                        orders.clear()
                        orders.addAll(fetchedOrders)
                        Log.d("OrdersDebug", "Fetched orders: ${gson.toJson(orders)}")
                        ordersLoaded.value = true
                        onResult(true, fetchedOrders)
                    } else {
                        Log.d("OrdersDebug", "No orders found for user $userId")
                        orders.clear()
                        ordersLoaded.value = true
                        onResult(false, null)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Firebase", "Failed to load orders", e)
                    ordersLoaded.value = true
                    onResult(false, null)
                }
        } else {
            ordersLoaded.value = true
            onResult(false, null)
        }
    }
}
