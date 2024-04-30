package com.example.cookbook

import android.os.Bundle
import android.widget.ScrollView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.cookbook.ui.theme.CookBookTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainActivityContent()
        }
    }
}

@Composable
fun MainActivityContent() {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            HeadContainer()

            Suggestions()

            MealCategories()

            MealView()
        }
    }
}

@Composable
fun HeadContainer() {
    Column(
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        Text(
            text = "Find and Cook",
            modifier = Modifier.padding(top = 25.dp)
        )
        Text(
            text = "Food for You",
            color = Color(0xFFFFA500) // Orange
        )
    }
}

@Composable
fun Suggestions() {
    Text(
        text = "Our suggestions",
        modifier = Modifier.padding(top = 28.dp)
    )
    // Suggestions content
}

@Composable
fun MealCategories() {
    Text(
        text = "Meal categories",
        modifier = Modifier.padding(top = 28.dp)
    )
    // Meal categories content
}

@Composable
fun MealView() {
    LazyColumn(
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        // Meal view content
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMainActivity() {
    MainActivityContent()
}