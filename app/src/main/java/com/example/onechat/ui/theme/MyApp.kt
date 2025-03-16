package com.example.onechat.ui.theme

import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.onechat.LoginDestination
import com.example.onechat.LoginScreen
import com.example.onechat.SignUpDestination
import com.example.onechat.SignUpScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun MyApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    NavHost(
        navController = navController,
        startDestination = if (currentUser == null) {
            LoginDestination.route
        } else {
            UserListDestination.route
        }
    ) {
        composable(LoginDestination.route) {
            LoginScreen(navController = navController)
        }
        composable(SignUpDestination.route) {
            SignUpScreen(navController = navController)
        }
        composable(UserListDestination.route) {
            UserListScreen(navController = navController)
        }
        composable("chat_screen/{chatid}/{receiverId}/{receiverEmail}") { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatid") ?: ""  // ✅ Fixed argument name
            val receiverId = backStackEntry.arguments?.getString("receiverId") ?: ""
            val receiverEmail = backStackEntry.arguments?.getString("receiverEmail") ?: ""

            ChatScreen(navController, chatId, receiverId, receiverEmail)
        }

    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    onBackClick:() -> Unit,
    isBackVisible: Boolean
) {
    TopAppBar(
        title = {
            Text(title)
        },
        navigationIcon = {
            if (isBackVisible){
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1976D2)) // Blue shade

    )
}


