package com.example.onechat.ui.theme

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.onechat.LoginDestination
import com.example.onechat.NavigationDestination
import com.example.onechat.R
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlin.math.absoluteValue

object UserListDestination : NavigationDestination {
    override val title = "User List"
    override val route = "user_list"
}


// ✅ User Model
data class User(
    val uid: String,
    val email: String,
    var lastMessage: String? = null,
    var lastMessageTimestamp: Long = 0L,
    var isUnread: Boolean = false
)

fun getChatId(user1: String?, user2: String?): String {
    return if (user1 != null && user2 != null) {
        if (user1 < user2) "${user1}_${user2}" else "${user2}_${user1}"
    } else {
        ""
    }
}



@Composable
fun UserListScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    // Load initial users
    LaunchedEffect(Unit) {
        listenForUserUpdates(db, auth, currentUser) { newUsers ->
            users = newUsers
            isLoading = false
        }
    }

    // Refresh function when pulling down
    fun refreshUsers() {
        isRefreshing = true
        listenForUserUpdates(db, auth, currentUser) { newUsers ->
            users = newUsers
            isRefreshing = false
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Friends",
                onBackClick = { },
                isBackVisible = false
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    auth.signOut()
                    navController.navigate(LoginDestination.route)
                },
                modifier = Modifier.padding(16.dp),
                shape = CircleShape,
                containerColor = Color(0xFF4CAF50),
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_logout_24),
                    contentDescription = "Logout",
                    tint = Color.White
                )
            }
        }
    ) { innerPadding ->
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing),
            onRefresh = { refreshUsers() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    if (users.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No users available", color = Color.Gray, fontSize = 18.sp)
                        }
                    } else {
                        LazyColumn(state = listState) {
                            items(users) { user ->
                                UserItem(user) {
                                    val senderId = currentUser?.uid ?: return@UserItem
                                    val receiverId = user.uid
                                    val chatId = getChatId(senderId, receiverId)

                                    markMessagesAsRead(db, chatId, senderId)

                                    Log.d(
                                        "UserListScreen",
                                        "Navigating to chat_screen/$chatId/$receiverId/${user.email}"
                                    )

                                    navController.navigate("chat_screen/$chatId/$receiverId/${user.email}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


fun listenForUserUpdates(
    db: FirebaseFirestore,
    auth: FirebaseAuth,
    currentUser: FirebaseUser?,
    onUsersFetched: (List<User>) -> Unit
) {
    Log.d("UserListScreen", "Fetching latest users...")

    val usersRef = db.collection("users")

    usersRef.get()
        .addOnSuccessListener { userSnapshot ->
            if (userSnapshot.isEmpty) {
                onUsersFetched(emptyList())  // 🔥 Ensure callback is called
                return@addOnSuccessListener
            }

            val userList = mutableListOf<User>()
            var usersProcessed = 0
            val totalUsers = userSnapshot.documents.size - 1  // Exclude current user

            for (userDoc in userSnapshot.documents) {
                val userId = userDoc.id
                if (userId == currentUser?.uid) continue  // Skip current user

                val user = User(
                    uid = userId,
                    email = userDoc.getString("email") ?: "",
                    lastMessage = null,
                    lastMessageTimestamp = 0L,
                    isUnread = false  // 🔥 Default to false
                )

                val chatId = getChatId(currentUser?.uid, userId)
                Log.d("UserListScreen", "Fetching last message for chatId: $chatId")

                db.collection("chats").document(chatId).collection("messages")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .addOnSuccessListener { chatSnapshot ->
                        if (!chatSnapshot.isEmpty) {
                            val lastMessageDoc = chatSnapshot.documents.first()
                            user.lastMessage = lastMessageDoc.getString("message") ?: ""
                            user.lastMessageTimestamp = lastMessageDoc.getTimestamp("timestamp")?.seconds ?: 0L
                        }

                        // 🔥 Check unread messages
                        db.collection("chats").document(chatId).collection("messages")
                            .whereEqualTo("receiverId", currentUser?.uid)
                            .whereEqualTo("isRead", false)
                            .get()
                            .addOnSuccessListener { unreadSnapshot ->
                                user.isUnread = !unreadSnapshot.isEmpty
                                Log.d("UserListScreen", "Unread status for ${user.email}: ${user.isUnread}")
                                userList.add(user)
                                usersProcessed++

                                if (usersProcessed == totalUsers) {
                                    Log.d("UserListScreen", "All users processed, updating UI")
                                    val updatedList = userList.sortedByDescending { it.lastMessageTimestamp }
                                    onUsersFetched(updatedList)  // ✅ Ensure callback is called
                                }
                            }
                    }
                    .addOnFailureListener {
                        usersProcessed++
                        if (usersProcessed == totalUsers) {
                            val updatedList = userList.sortedByDescending { it.lastMessageTimestamp }
                            onUsersFetched(updatedList)  // ✅ Handle failures gracefully
                        }
                    }
            }

            if (totalUsers == 0) {
                onUsersFetched(emptyList())  // ✅ If no users exist, update UI immediately
            }
        }
        .addOnFailureListener { e ->
            Log.e("UserListScreen", "Error fetching users", e)
            onUsersFetched(emptyList())  // ✅ Handle Firestore errors properly
        }
}






@Composable
fun UserItem(
    user: User,
    onClick: () -> Unit
) {
    val truncatedMessage = user.lastMessage?.take(30) ?: "No messages yet"
    val isUnread = user.isUnread

    val userName = user.email.substringBefore("@").replaceFirstChar { it.uppercase() } // Extract and capitalize name
    val firstLetter = userName.firstOrNull()?.toString()?.uppercase() ?: "?"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clickable { onClick() }
                .background(if (isUnread) Color.LightGray.copy(alpha = 0.3f) else Color.Transparent),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(getRandomColor(user.uid), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.email.first().uppercaseChar().toString(),
                    color = Color.White,
                    fontSize = 20.sp
                )
            }

            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(text = userName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    text = truncatedMessage,
                    fontSize = 14.sp,
                    fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Normal,
                    color = if (isUnread) Color.Black else Color.Gray
                )
            }
        }
    }

}


fun markMessagesAsRead(db: FirebaseFirestore, chatId: String, currentUserId: String) {
    val messagesRef = db.collection("chats").document(chatId).collection("messages")

    messagesRef.whereEqualTo("receiverId", currentUserId)
        .whereEqualTo("isRead", false)  // 🔥 Only update unread messages
        .get()
        .addOnSuccessListener { snapshot ->
            val batch = db.batch()
            for (doc in snapshot.documents) {
                batch.update(doc.reference, "isRead", true)
            }
            batch.commit().addOnSuccessListener {
                Log.d("UserListScreen", "Marked messages as read")
            }.addOnFailureListener { e ->
                Log.e("UserListScreen", "Failed to mark messages as read", e)
            }
        }
        .addOnFailureListener { e ->
            Log.e("UserListScreen", "Error fetching unread messages", e)
        }
}


// Function to generate a random color based on user ID or email hash
fun getRandomColor(userIdentifier: String): Color {
    val colors = listOf(
        Color(0xFFE57373), // Red
        Color(0xFFF06292), // Pink
        Color(0xFFBA68C8), // Purple
        Color(0xFF64B5F6), // Blue
        Color(0xFF4DB6AC), // Teal
        Color(0xFF81C784), // Green
        Color(0xFFFFD54F), // Yellow
        Color(0xFFA1887F), // Brown
    )
    val index = userIdentifier.hashCode().absoluteValue % colors.size
    return colors[index]
}