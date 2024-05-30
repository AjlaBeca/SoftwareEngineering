package com.example.cookbook.ui.screens

import android.content.res.Resources
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.cookbook.R
import com.example.cookbook.data.viewmodels.UserViewModel
import com.example.cookbook.ui.theme.Orange
import com.example.cookbook.ui.theme.White

@Composable
fun HomeScreen(navController: NavHostController, userViewModel: UserViewModel) {
    val currentUser by userViewModel.currentUser.observeAsState(null)
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        //horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val text = buildAnnotatedString {
            withStyle(style = MaterialTheme.typography.headlineLarge.toSpanStyle().copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)) {
                append("Hello, ")
            }

            withStyle(style = MaterialTheme.typography.headlineLarge.toSpanStyle().copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)) {
                append(currentUser?.username ?: "")
            }
        }

        Text(
            text = text,
            modifier = Modifier.padding(30.dp)
        )
        CategoryButton(navController, "Breakfast", R.drawable.breakfast)
        Spacer(modifier = Modifier.height(30.dp))
        CategoryButton(navController, "Lunch", R.drawable.lunch)
        Spacer(modifier = Modifier.height(30.dp))
        CategoryButton(navController, "Dinner", R.drawable.dinner)
        Spacer(modifier = Modifier.height(30.dp))
        CategoryButton(navController, "Dessert", R.drawable.dessert)
        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
fun CategoryButton(navController: NavHostController, category: String, imageResId: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp) // Set a fixed height for consistent button size
            .padding(horizontal = 20.dp)
    ) {
        Image(
            painter = painterResource(id = imageResId), // Load the image using painterResource
            contentDescription = null,
            contentScale = ContentScale.Crop, // Crop the image to fill the space
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(10.dp))
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(10.dp))
                .background(Color.Black.copy(alpha = 0.4f)) // Semi-transparent overlay
                .padding(vertical = 20.dp)
        )
        Button(
            onClick = { navController.navigate("listScreen/$category") },
            modifier = Modifier
                .fillMaxSize()
                .padding(30.dp)
                .clip(RoundedCornerShape(10.dp)),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = White
            )
        ) {
            Text(
                text = category,
                color = White,
                style = MaterialTheme.typography.headlineLarge
            )
        }
    }
}
