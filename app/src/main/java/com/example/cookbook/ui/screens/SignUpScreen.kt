package com.example.cookbook.ui.screens

import android.util.Patterns
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import androidx.lifecycle.viewModelScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.cookbook.R
import com.example.cookbook.data.models.User
import com.example.cookbook.data.viewmodels.UserViewModel
import com.example.cookbook.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(navController: NavController, userViewModel: UserViewModel, modifier: Modifier = Modifier) {
    val userViewModel: UserViewModel = viewModel()
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }


    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                color = Gray,
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(375.dp)
                .padding(16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.cookbook_logo_white),
                contentDescription = "CookBook Logo",
                modifier = Modifier
                    .align(alignment = Alignment.CenterHorizontally)
                    .requiredWidth(236.dp)
                    .requiredHeight(182.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = White,
                    focusedIndicatorColor = Orange,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Gray,
                    cursorColor = Orange,
                    focusedLabelColor = Orange
                ),
                shape = MaterialTheme.shapes.medium
            )
            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                isError = email.isEmpty() && errorMessage.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = White,
                    focusedIndicatorColor = Orange,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Gray,
                    cursorColor = Orange,
                    focusedLabelColor = Orange
                ),
                shape = MaterialTheme.shapes.medium
            )
            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                isError = password.isEmpty() && errorMessage.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = White,
                    focusedIndicatorColor = Orange,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Gray,
                    cursorColor = Orange,
                    focusedLabelColor = Orange
                ),
                shape = MaterialTheme.shapes.medium
            )

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(alignment = Alignment.CenterHorizontally)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    userViewModel.viewModelScope.launch {
                        when {
                            username.isEmpty() || email.isEmpty() || password.isEmpty() -> {
                                errorMessage = "All fields must be filled out"
                            }
                            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                                errorMessage = "Please enter a valid email"
                            }
                            else -> {
                                // Handle sign up
                                errorMessage = ""
                                val signUpSuccess = userViewModel.addUser(User(email = email, password = password, username = username, image = null))
                                if (signUpSuccess) {
                                    navController.navigate("home")
                                } else {
                                    // Handle sign-up failure
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Orange
                )
            ) {
                Text("Sign Up", color = Color.White) // Set text color here
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = { navController.navigate("login") },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Already have an account? Sign In", color = White)
            }

            TextButton(
                onClick = { /* handle forgot password */ },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Forgot password?", color = DarkOrange)
            }
        }
    }
}