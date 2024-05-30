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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.cookbook.R
import com.example.cookbook.data.models.User
import com.example.cookbook.data.viewmodels.UserViewModel
import com.example.cookbook.ui.theme.*
import com.example.cookbook.utils.SharedPreferencesUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    navController: NavController,
    userViewModel: UserViewModel,
    modifier: Modifier = Modifier
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    fun isPasswordValid(password: String): Boolean {
        val passwordRegex = "^(?=.*[0-9])(?=.*[A-Z]).{6,}$".toRegex()
        return passwordRegex.matches(password)
    }

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

                            !isPasswordValid(password) -> {
                                errorMessage = "Password must be at least 6 characters long, contain at least one number and one uppercase letter"
                            }

                            else -> {
                                errorMessage = ""
                                val existingUser = userViewModel.getUserByEmail(email)
                                if (existingUser == null) {
                                    val signUpSuccess = userViewModel.addUser(
                                        User(
                                            email = email,
                                            password = password,
                                            username = username,
                                            image = null
                                        )
                                    )
                                    if (signUpSuccess) {
                                        val user = userViewModel.getUserByEmail(email)
                                        if (user != null) {
                                            SharedPreferencesUtil.setLoggedIn(
                                                navController.context,
                                                true,
                                                user.userId
                                            )
                                            navController.navigate("login")
                                        } else {
                                            errorMessage = "Failed to sign up. Please try again."
                                        }
                                    } else {
                                        errorMessage = "Failed to sign up. Please try again."
                                    }
                                } else {
                                    errorMessage = "Email already in use. Please log in."
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
                Text(
                    "Sign Up", color = Color.White,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = { navController.navigate("login") },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Already have an account? Sign In", color = White)
            }
        }
    }
}
