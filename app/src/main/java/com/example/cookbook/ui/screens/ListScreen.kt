package com.example.cookbook.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.cookbook.data.models.Recipe
import com.example.cookbook.ui.theme.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.example.cookbook.data.viewmodels.RecipeViewModel

@Composable
fun ListScreen(navController: NavHostController, category: String?, recipeViewModel: RecipeViewModel) {
    val categories = mapOf("Breakfast" to 1, "Lunch" to 2, "Dinner" to 3, "Dessert" to 4)
    val categoryId = category?.let { categories[it] }
    val allRecipes by (categoryId?.let { recipeViewModel.getRecipesByCategory(it) } ?: recipeViewModel.getAllRecipes()).observeAsState(listOf())

    var searchText by remember { mutableStateOf("") }

    // Filter recipes based on search text
    val filteredRecipes = allRecipes.filter { recipe ->
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
        Button(
            onClick = { navController.navigate("add_screen") },
            modifier = Modifier
                .padding(top = 8.dp)
                .padding(horizontal = 4.dp)
                .align(Alignment.CenterHorizontally),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(containerColor = Orange, contentColor = White),
        ) {
            Text("Add Recipe")
        }
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
                        RecipeItem(recipe = recipe, inGrid = true, navController = navController, modifier = Modifier.weight(1f))
                        if (index < rowRecipes.size - 1) {
                            Spacer(modifier = Modifier.width(16.dp))
                        }
                    }
                    // If there's only one recipe in the pair, add an empty space
                    if (rowRecipes.size < 2) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun RecipeItem(recipe: Recipe, modifier: Modifier = Modifier, navController: NavHostController, inGrid: Boolean =false) {
    var isLiked by remember { mutableStateOf(false) }
    val isDarkTheme = isSystemInDarkTheme()
    Box(
        modifier = modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
            .clickable {
                navController.navigate("recipe_detail_screen/${recipe.recipeId}")
            }
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape = RoundedCornerShape(10.dp))
                .background(DarkerGray)
                .border(border = BorderStroke(1.dp, Gray), shape = RoundedCornerShape(10.dp)),
        ) {
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.background(if (isDarkTheme) Gray else LighterGray)
            ) {
                recipe.imagePath?.let { imagePath ->
                    Image(
                        painter = rememberAsyncImagePainter(imagePath),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentScale = ContentScale.FillWidth
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = recipe.name,
                    color = White,
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp),
                    textAlign = TextAlign.Start
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("Time: ")
                        }
                        append(recipe.time)
                    },
                    color = White,
                    style = TextStyle(fontSize = 12.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp),
                    textAlign = TextAlign.Start
                )
                Text(
                    buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("Complexity: ")
                        }
                        append(recipe.complexity)
                    },
                    color = White,
                    style = TextStyle(fontSize = 12.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp),
                    textAlign = TextAlign.Start
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    CompositionLocalProvider(LocalContentColor provides if (isLiked) Orange else White) {
                        IconButton(
                            onClick = { isLiked = !isLiked }, // Toggle the like state when clicked
                        ) {
                            Icon(
                                imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Like Recipe",
                                modifier = Modifier.size(24.dp) // Increase heart icon size
                            )
                        }
                    }
                }
            }
        }
    }
}