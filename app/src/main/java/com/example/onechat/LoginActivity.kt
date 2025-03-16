package com.example.onechat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.onechat.ui.theme.UserListDestination
import com.google.firebase.auth.FirebaseAuth


object LoginDestination : NavigationDestination {
    override val title = "Login"
    override val route = "login"
}

@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .background(Color(0xFFF8F9FA)), // Light background for a clean look
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Login",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Button(
            onClick = {
                isLoading = true
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        isLoading = false
                        if (task.isSuccessful) {
                            Log.d("LoginScreen", "Login successful")
                            Toast.makeText(context, "Login Successful 🎉", Toast.LENGTH_SHORT).show()
                            navController.navigate(UserListDestination.route) {
                                popUpTo(LoginDestination.route) { inclusive = true }
                            }
                        } else {
                            Log.e("LoginScreen", "Login failed", task.exception)
                            Toast.makeText(context, "Login Failed: $errorMessage", Toast.LENGTH_LONG).show()
                            errorMessage = task.exception?.message ?: "An error occurred"
                        }
                    }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007BFF)) // Blue button
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text("Login", fontSize = 18.sp, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = { navController.navigate(SignUpDestination.route) }
        ) {
            Text("Don't have an account? Sign up", color = Color.Blue)
        }
    }
}



// kashif@gmail.com