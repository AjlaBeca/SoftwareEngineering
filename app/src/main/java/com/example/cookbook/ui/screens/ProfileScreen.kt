package com.example.cookbook.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.cookbook.data.viewmodels.UserViewModel
import com.example.cookbook.utils.SharedPreferencesUtil
import kotlinx.coroutines.launch
@Composable
fun ProfileScreen(navController: NavHostController, userViewModel: UserViewModel) {
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text("Profile Screen")
        // Add more content here

        TextButton(
            onClick = {
                coroutineScope.launch {
                    userViewModel.logout()
                    SharedPreferencesUtil.setLoggedIn(navController.context, false)
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text(
                "Logout",
                color = Color.Red,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
