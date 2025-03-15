package com.example.onechat.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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