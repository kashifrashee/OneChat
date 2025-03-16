package com.example.onechat.ui.theme

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.onechat.NavigationDestination
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale


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
    chatId: String,
    receiverId: String,
    receiverEmail: String
) {
    var messageText by remember { mutableStateOf(TextFieldValue("")) }
    var messages by remember { mutableStateOf(listOf<Messages>()) }
    val listState = rememberLazyListState() // ✅ Auto-scroll
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val scopedMessage = rememberCoroutineScope()


    // ✅ Fetch Messages from Firestore
    LaunchedEffect(chatId) {
        val chatRef = db.collection("chats")
            .document(chatId)
            .collection("messages")

        chatRef.orderBy("timestamp")
            .addSnapshotListener { snapshot, exception ->
                if (exception == null && snapshot != null) {
                    val fetchedMessages = snapshot.documents.map {
                        Messages(
                            message = it.getString("message") ?: "",
                            senderId = it.getString("senderId") ?: "",
                            timestamp = it.getTimestamp("timestamp")?.toDate()?.let { date ->
                                SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date)
                            } ?: ""
                        )
                    }
                    messages = fetchedMessages

                    // ✅ Auto-scroll only if there are messages
                    if (messages.isNotEmpty()) {
                        scopedMessage.launch {
                            listState.animateScrollToItem(messages.size - 1)
                        }
                    }
                }
            }
    }

    val receiverName = receiverEmail.substringBefore("@")
        .replaceFirstChar { it.uppercase() } // Extract and capitalize name

    Scaffold(
        topBar = {
            AppTopBar(
                title = receiverName,
                onBackClick = { navController.popBackStack() },
                isBackVisible = true
            )
        } // ✅ Top bar
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(5.dp)
                .fillMaxSize()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
                    .padding(top = 106.dp), // ✅ Adjust this based on your TopBar height
                reverseLayout = false // Messages appear top-to-bottom
            ) {
                items(messages) { message ->
                    val isCurrentUser = message.senderId == currentUser?.uid
                    MessageItem(message, isCurrentUser)
                }
            }

            // ✅ Sticky Message Input Box
            MessageInputField(
                messageText = messageText,
                onMessageChange = { messageText = it },
                onSendClick = {
                    if (messageText.text.isNotEmpty()) {
                        scopedMessage.launch {
                            val chatRef = db.collection("chats").document(chatId)
                            val messagesRef = chatRef.collection("messages")

                            val messageData = mapOf(
                                "message" to messageText.text,
                                "senderId" to currentUser?.uid,
                                "receiverId" to receiverId,
                                "timestamp" to com.google.firebase.Timestamp.now()
                            )

                            // ✅ Add Message to Firestore
                            messagesRef.add(messageData).addOnSuccessListener {
                                Log.d("ChatScreen", "Message sent successfully $messageData")
                                messageText = TextFieldValue("")

                                // ✅ Update Chat Metadata in `/chats/{chatId}`
                                chatRef.update(
                                    "lastMessage", messageData["message"],
                                    "lastMessageTimestamp", messageData["timestamp"],
                                    "lastMessageSenderId", messageData["senderId"]
                                ).addOnFailureListener {
                                    // If document doesn't exist, create it
                                    chatRef.set(
                                        mapOf(
                                            "lastMessage" to messageData["message"],
                                            "lastMessageTimestamp" to messageData["timestamp"],
                                            "lastMessageSenderId" to messageData["senderId"]
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

            )
        }
    }
}

// ✅ Improved Message Bubble UI
@Composable
fun MessageItem(message: Messages, isCurrentUser: Boolean) {
    Row(
        modifier = Modifier
            .padding(3.dp)
            .fillMaxWidth(),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isCurrentUser) Color(0xFF4CAF50) else Color(0xFF2196F3),
            modifier = Modifier
                .padding(8.dp)
                .widthIn(max = 280.dp)
                .shadow(2.dp, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.message,
                    fontSize = 16.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.timestamp,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

// ✅ Sticky Input Box with Rounded Corners & Shadow
@Composable
fun MessageInputField(
    messageText: TextFieldValue,
    onMessageChange: (TextFieldValue) -> Unit,
    onSendClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .background(Color.White),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = messageText,
            onValueChange = onMessageChange,
            placeholder = { Text("Type a message...") },
            singleLine = true,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            shape = RoundedCornerShape(50)
        )


        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
            onClick = onSendClick,
            modifier = Modifier
                .size(48.dp)
                .background(Color(0xFF4CAF50), shape = CircleShape),
            enabled = messageText.text.isNotEmpty()
        ) {
            Icon(
                imageVector = Icons.Filled.Send,
                contentDescription = "Send",
                tint = Color.White,
            )
        }
    }
}
