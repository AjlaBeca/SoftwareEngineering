package com.example.cookbook.ui.screens

import android.util.Patterns
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.cookbook.R
import com.example.cookbook.data.viewmodels.UserViewModel
import com.example.cookbook.ui.theme.*
import com.example.cookbook.utils.SharedPreferencesUtil
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController, userViewModel: UserViewModel, modifier: Modifier = Modifier) {
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
                visualTransformation = PasswordVisualTransformation(),
                isError = password.isEmpty() && errorMessage.isNotEmpty(),
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
            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Button(
                onClick = {
                    when {
                        email.isEmpty() || password.isEmpty() -> {
                            errorMessage = "All fields must be filled out"
                        }
                        !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                            errorMessage = "Please enter a valid email"
                        }
                        else -> {
                            userViewModel.viewModelScope.launch {
                                val user = userViewModel.login(email, password)
                                if (user != null) {
                                    SharedPreferencesUtil.setLoggedIn(navController.context, true, user.userId)
                                    navController.navigate("home")
                                } else {
                                    errorMessage = "Invalid email or password"
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = Orange)
            ) {
                Text("Login", color = Color.White)
            }
            TextButton(
                onClick = { navController.navigate("signup") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = ButtonDefaults.textButtonColors(contentColor = White)
            ) {
                Text("Don't have an account? Sign Up")
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
