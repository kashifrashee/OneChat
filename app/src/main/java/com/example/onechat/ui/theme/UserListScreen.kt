package com.example.onechat.ui.theme

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.onechat.NavigationDestination
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object UserListDestination : NavigationDestination {
    override val title = "User List"
    override val route = "user_list"
}


// User data model
data class User(val uid: String, val email: String)


@Composable
fun UserListScreen(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    var users by remember { mutableStateOf<List<User>>(emptyList()) }

    // Fetch users from Firestore
    LaunchedEffect(Unit) {
        db.collection("users").get()
            .addOnSuccessListener { result ->
                val userList = result.documents.mapNotNull { doc ->
                    val uid = doc.getString("uid")
                    val email = doc.getString("email")
                    if (uid != currentUser?.uid) { // Exclude logged-in user
                        User(uid ?: "", email ?: "")
                    } else null
                }
                users = userList
                Log.d("UserListScreen", "Users fetched: $users")
            }
            .addOnFailureListener { exception ->
                Log.e("UserListScreen", "Error fetching users: ${exception.message}")
            }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Users", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        LazyColumn {
            items(users) { user ->
                UserItem(user) {
                    val senderId = currentUser?.uid ?: return@UserItem
                    val receiverId = user.uid

                    // ✅ Corrected chatId format
                    val chatid = if (senderId < receiverId) "${senderId}_${receiverId}" else "${receiverId}_${senderId}"

                    Log.d("UserListScreen", "Navigating to chat_screen/$chatid/$receiverId/${user.email}")

                    navController.navigate("chat_screen/$chatid/$receiverId/${user.email}")
                }
            }
        }
    }
}


// Composable for displaying a single user
@Composable
fun UserItem(user: User, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        Text(
            text = user.email,
            modifier = Modifier.padding(16.dp),
            fontSize = 18.sp
        )
    }
}