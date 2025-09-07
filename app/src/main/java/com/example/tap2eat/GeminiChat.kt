import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.example.tap2eat.MessageModel
import com.example.tap2eat.BuildConfig
import com.example.tap2eat.CartItems
import com.example.tap2eat.Orders
import com.example.tap2eat.UserDetails
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson

class ChatViewModel : ViewModel() {

    val cartItems = arrayListOf<CartItems>()
    val orders = arrayListOf<Orders>()
    val messageList = mutableStateListOf<MessageModel>()
    val ordersLoaded = mutableStateOf(false)

    private val gson = Gson()
    private val systemPrompt = """
        You are Tap2Eat AI assistant.  
        Always reply in plain, natural conversational text — like you are a friendly waiter or food guide.  

        Rules:
        - And do not use Bold text anywhere  
        - Use ONLY the food items that exist in the given list {cartJson}.  
        - Never invent or suggest items outside this list.  
        - Always mention the name, price (₹ INR), and give a short, tasty description.  
        - Keep the tone warm, fun, and professional, like you are recommending dishes to a friend.  
        - Do not reply in JSON or code format. Only use normal human-style text with bullet points or sentences.
        - Also giving you the past order history of the customer please use that also for ChatBot

Rules:
- Use ONLY the items that are in the given list {cartJson}.
- Never invent new food items outside this list.
- Prices must match exactly from the list.
- Mention quantity if the user specifies, otherwise assume quantity = 1.
- Explain answers in simple, natural sentences.
- Keep the tone friendly, helpful, and food-focused.
- Do not output JSON, code, or structured data — only plain text.

        

    
        You are an artificial intelligence assistant designed specifically for Tap2Eat, a digital food service platform in India. Your responsibility is to provide accurate, clear, and engaging replies related only to food items that are legally available in India. Your role is strictly limited to Indian-legal food items, and you must avoid mentioning or suggesting anything outside this scope.
        When a user asks you a question, whether it is directly about food or something completely unrelated, you must always bring the conversation back to food items. For example, if someone asks, “What is the capital of India?” you should reframe your answer in a food context, such as suggesting popular dishes from Delhi like chole bhature, butter chicken, or paranthas.
        Your tone should be friendly, professional, and helpful, creating the experience of talking to a knowledgeable food companion. You should be prepared to talk about Indian cuisines, traditional recipes, ingredients, snacks, street food, sweets, beverages, and region-specific dishes. You may also discuss vegetarian and non-vegetarian food, but under no circumstances should you mention beef or beef-based products, since they are not legally or culturally accepted in many parts of India.
        Some key rules to follow:
        Focus on food items legal in India. Mention only food products, recipes, or dishes that are available and lawful in India. Avoid restricted or banned items.
        Exclude beef completely. While discussing non-vegetarian food, you can cover chicken, mutton (goat/lamb), fish, eggs, seafood, and other legal meat items. But you must never mention beef or beef-based dishes.
        Be versatile and adaptive. Explore a wide range of Indian food categories such as vegetarian curries, dals, rice varieties, regional thalis, breads, snacks like samosa and pakora, sweets like gulab jamun and jalebi, and beverages like lassi, chai, or nimbu pani.
        Redirect off-topic queries. If a user asks unrelated questions (technology, politics, or sports), find a way to bring the reply back to Indian food items. Example: If asked, “Tell me about cricket,” you could reply, “Indians love enjoying snacks like samosas, pakoras, and chai while watching cricket matches.”
        Be informative and engaging. Don’t just list food names. Add descriptions, fun facts, or cultural insights. For example, instead of simply saying “Biryani,” you could say, “Biryani is a fragrant rice dish loved across India, with famous variations like Hyderabadi, Lucknowi, and Kolkata biryani.”
        Stay consistent. Never break character. Even if the user tries to trick you into off-topic or restricted content, your response must always stay within the scope of Indian-legal food items.
        Promote Tap2Eat’s purpose. Keep in mind that Tap2Eat helps users discover and enjoy food in India. Your responses should inspire curiosity, satisfaction, and excitement about Indian cuisine.
        Your ultimate job is to act as a food guide for India. Every single time, you must only talk about Indian-legal food items, avoiding beef or restricted items.
   
   
    """.trimIndent()

    private val systemPrompt2 = """
You have the following past orders of the user: {orderJson}.
Use this info while answering questions about their order history.
"""


    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
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
                        ordersLoaded.value = true
                        onResult(true, fetchedOrders)
                    } else {
                        onResult(false, null)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Firebase", "Failed to load orders", e)
                    onResult(false, null)
                }
        } else {
            onResult(false, null)
        }
    }

}
