package com.example.onechat.ui.theme

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.onechat.NavigationDestination
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch


object ChatDestination : NavigationDestination {
    override val title = "Chat"
    override val route = "chat"
}

data class Messages(
    val message: String,
    val senderId: String,
    val timestamp: String
)

@Composable
fun ChatScreen(
    navController: NavController,
    chatId: String,  // <-- Add chatId parameter
    receiverId: String,
    receiverEmail: String,
    modifier: Modifier = Modifier
) {
    var messageText by remember { mutableStateOf(TextFieldValue("")) }
    var messages by remember { mutableStateOf(listOf<Messages>()) }

    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val scopedMessage = rememberCoroutineScope()


    // ✅ Corrected Fetching Messages Code
    LaunchedEffect(chatId) {
        val chatRef = db.collection("chats").document(chatId).collection("messages")

        chatRef.orderBy("timestamp")
            .addSnapshotListener { snapshot, exception ->
                if (exception == null && snapshot != null) {
                    val fetchedMessages = snapshot.documents.map {
                        Messages(
                            message = it.getString("message") ?: "",
                            senderId = it.getString("senderId") ?: "",
                            timestamp = it.getTimestamp("timestamp")?.toDate().toString()
                        )
                    }
                    messages = fetchedMessages
                    Log.d("ChatScreen", "Fetched messages: $messages")
                } else {
                    Log.e("ChatScreen", "Error fetching messages: ${exception?.message}")
                }
            }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Text(
            text = "Chat with $receiverEmail",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(messages.size) { index ->
                val message = messages[index]
                val isCurrentUser = message.senderId == currentUser?.uid
                MessageItem(message, isCurrentUser)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ✅ Improved Text Input Field
            TextField(
                value = messageText.text,
                onValueChange = { messageText = TextFieldValue(it) },
                modifier = Modifier
                    .weight(1f)
                    .background(Color.White, CircleShape)
                    .padding(12.dp),
                placeholder = { Text("Type a message...") }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    val message = messageText.text
                    if (message.isNotEmpty()) {
                        scopedMessage.launch {
                            val chatRef = db
                                .collection("chats")
                                .document(chatId)
                                .collection("messages")

                            chatRef.add(
                                mapOf(
                                    "message" to message,
                                    "senderId" to currentUser?.uid,
                                    "receiverId" to receiverId,
                                    "timestamp" to com.google.firebase.Timestamp.now()
                                )
                            )
                                .addOnSuccessListener {
                                    Log.d("ChatScreen", "Message sent successfully")
                                    messageText = TextFieldValue("")
                                }
                                .addOnFailureListener { exception ->
                                    Log.e("ChatScreen", "Error sending message", exception)
                                }
                        }
                    }
                }
            ) {
                Text("Send")
            }
        }
    }
}


@Composable
fun MessageItem(message: Messages, isCurrentUser: Boolean) {
    val alignment =
        if (isCurrentUser)
            Alignment.End
        else
            Alignment.Start
    val backgroundColor =
        if (isCurrentUser)
            MaterialTheme.colorScheme.primary
        else Color.Gray

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = alignment
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            shadowElevation = 1.dp,
            color = backgroundColor
        ) {
            Text(
                text = "${message.message}\n${message.timestamp}",
                fontSize = 16.sp,
                color = Color.White,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}