package com.example.tap2eat

import ChatViewModel
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class Gemini : ComponentActivity() {
    private val chatViewModel by viewModels<ChatViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chatViewModel.loadItems(this) { success ->
        }

        val profile=intent.getSerializableExtra("EXTRA_USER_DETAILS") as? UserDetails

        profile?.let {
            chatViewModel.loadUserOrders(this, it) { success, orders -> }
        }
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ChatPage(chatViewModel)
                }
            }
        }
    }
}

@Composable
fun ChatPage(viewModel: ChatViewModel) {
    Column(modifier = Modifier.padding(horizontal = 15.dp).padding(bottom = 80.dp).padding(top = 20.dp)) {
        AppHeader()
        MessageList(
            messages = viewModel.messageList,
            modifier = Modifier.weight(1f).padding(8.dp)
        )
        MessageInput { msg ->
            if (msg.isNotBlank()) viewModel.sendMessage(msg)
        }
    }
}

@Composable
fun AppHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("Tap2Eat", fontSize = 22.sp, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun MessageList(messages: List<MessageModel>, modifier: Modifier = Modifier) {
    if (messages.isEmpty()) {
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Ask me anything", fontSize = 20.sp)
        }
    } else {
        LazyColumn(modifier = modifier, reverseLayout = true) {
            items(messages.reversed()) { message ->
                MessageRow(message)
            }
        }
    }
}

@Composable
fun MessageRow(message: MessageModel) {
    val isModel = message.role == "model"
    val bubbleColor = if (isModel) Color(0xFFEDEDED) else Color(0xFF008577)
    val textColor = if (isModel) Color.Black else Color.White

    Row(
        modifier = Modifier.fillMaxWidth().padding(4.dp),
        horizontalArrangement = if (isModel) Arrangement.Start else Arrangement.End
    ) {
        Surface(
            color = bubbleColor,
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 2.dp,
            shadowElevation = 2.dp
        ) {
            SelectionContainer {
                Text(
                    message.message,
                    modifier = Modifier.padding(12.dp),
                    color = textColor
                )
            }
        }
    }
}

@Composable
fun MessageInput(onMessageSend: (String) -> Unit) {
    var message by remember { mutableStateOf("") }

    Row(
        modifier = Modifier.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            modifier = Modifier.weight(1f),
            placeholder = { Text("Ask anything...") }
        )
        IconButton(onClick = {
            onMessageSend(message)
            message = ""
        }) {
            Icon(Icons.Default.Send, contentDescription = "Send")
        }
    }
}
