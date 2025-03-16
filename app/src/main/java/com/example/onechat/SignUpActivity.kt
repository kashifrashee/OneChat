package com.example.onechat

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object SignUpDestination : NavigationDestination {
    override val title = "Sign Up"
    override val route = "sign_up"
}

@Composable
fun SignUpScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Sign Up", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.Black)

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (password == confirmPassword) {
                    isLoading = true
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            isLoading = false
                            if (task.isSuccessful) {
                                val user = task.result?.user
                                user?.let {
                                    val userMap = mapOf(
                                        "uid" to it.uid,
                                        "email" to it.email
                                    )
                                    firestore.collection("users")
                                        .document(it.uid)
                                        .set(userMap)
                                        .addOnSuccessListener {
                                            Toast.makeText(context, "Account Created Successfully! 🎉", Toast.LENGTH_SHORT).show()
                                            navController.navigate(LoginDestination.route)
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("SignUp", "Error saving user", e)
                                            errorMessage = "Failed to save user to Firestore"
                                            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                        }
                                }
                            } else {
                                errorMessage = task.exception?.message ?: "An error occurred"
                                Toast.makeText(context, "Sign-Up Failed: $errorMessage", Toast.LENGTH_LONG).show()
                            }
                        }
                } else {
                    errorMessage = "Passwords do not match"
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007BFF)),
            enabled = !isLoading && email.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank()
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Sign Up", fontSize = 18.sp, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { navController.navigate(LoginDestination.route) }) {
            Text("Already have an account? Log in", color = Color.Blue)
        }
    }
}
