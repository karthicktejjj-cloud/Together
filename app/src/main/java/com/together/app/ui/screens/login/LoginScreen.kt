package com.together.app.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.together.app.ui.components.TogetherButton
import com.together.app.ui.theme.TogetherBackground
import com.together.app.ui.theme.TogetherPrimary
import com.together.app.ui.theme.TogetherSurface

@Composable
fun LoginScreen(navController: NavHostController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TogetherBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Sign in to continue your movie night.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(48.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TogetherPrimary,
                    unfocusedBorderColor = TogetherSurface,
                    focusedLabelColor = TogetherPrimary,
                    unfocusedLabelColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TogetherPrimary,
                    unfocusedBorderColor = TogetherSurface,
                    focusedLabelColor = TogetherPrimary,
                    unfocusedLabelColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            TogetherButton(
                text = "Login",
                onClick = {
                    navController.navigate("home")
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(
                onClick = { /* Sign Up */ }
            ) {
                Text(
                    text = "Don't have an account? Create one",
                    color = Color.Gray,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
