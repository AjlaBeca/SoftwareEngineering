package com.example.cookbook.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.cookbook.data.viewmodels.RecipeViewModel
import com.example.cookbook.data.viewmodels.UserViewModel
import com.example.cookbook.ui.theme.*
import com.example.cookbook.utils.SharedPreferencesUtil
import kotlinx.coroutines.launch
@Composable
fun ProfileScreen(
    navController: NavHostController,
    userViewModel: UserViewModel,
    recipeViewModel: RecipeViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    val currentUserId by userViewModel.currentUserId.observeAsState()

    var searchText by remember { mutableStateOf("") }

    // Fetch recipes for the current user
    val userRecipes by recipeViewModel.getRecipesByUser(currentUserId ?: -1).observeAsState(listOf())

    // Filter recipes based on search text
    val filteredRecipes = userRecipes.filter { recipe ->
        searchText.isEmpty() || recipe.name.contains(searchText, ignoreCase = true)
    }

    Spacer(modifier = Modifier.height(16.dp))
    Column {
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            label = { Text("Search Recipes") },
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(
            contentPadding = PaddingValues(8.dp),
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            items(filteredRecipes.chunked(2)) { rowRecipes ->
                Row(
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    rowRecipes.forEachIndexed { index, recipe ->
                        RecipeItem(
                            recipe = recipe,
                            inGrid = true,
                            navController = navController,
                            modifier = Modifier.weight(1f)
                        )
                        if (index < rowRecipes.size - 1) {
                            Spacer(modifier = Modifier.width(16.dp))
                        }
                    }
                    if (rowRecipes.size < 2) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

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