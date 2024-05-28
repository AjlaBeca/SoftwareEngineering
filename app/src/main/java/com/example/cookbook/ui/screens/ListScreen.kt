package com.example.cookbook.ui.screens

import RecipeViewModel
import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.navigation.NavHostController
import com.example.cookbook.data.models.Recipe
import com.example.cookbook.ui.theme.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.*
import androidx.lifecycle.LiveData
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import coil.request.ImageRequest
import com.example.cookbook.R
import com.example.cookbook.data.models.Favourite
import com.example.cookbook.data.models.User
import com.example.cookbook.data.viewmodels.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    navController: NavHostController,
    category: String?,
    recipeViewModel: RecipeViewModel,
    userViewModel: UserViewModel,
    userId: Long
) {
    val categories = mapOf("Breakfast" to 1, "Lunch" to 2, "Dinner" to 3, "Dessert" to 4)
    val categoryId = category?.let { categories[it] }
    val allRecipes by (categoryId?.let { recipeViewModel.getRecipesByCategory(it) }
        ?: recipeViewModel.getAllRecipes()).observeAsState(listOf())
    var searchText by remember { mutableStateOf("") }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var recipeToDelete by remember { mutableStateOf<Recipe?>(null) }

    val filteredRecipes = allRecipes.filter { recipe ->
        searchText.isEmpty() || recipe.name.contains(searchText, ignoreCase = true)
    }.filterNot { recipe ->
        recipe.authorId == userId
    }

    LaunchedEffect(key1 = userId) {
        filteredRecipes.forEach { recipe ->
            recipeViewModel.fetchInitialFavouriteStatus(recipe.recipeId ?: 0, userId)
        }
    }


    Spacer(modifier = Modifier.height(30.dp))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Show back button only if a category is specified
            if (category != null) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }

            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = { Text("Search") },
                shape = MaterialTheme.shapes.medium,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    cursorColor = White,
                    focusedBorderColor = LighterGray,
                    unfocusedBorderColor = LighterGray,
                    focusedLabelColor = LighterGray,
                    unfocusedLabelColor = LighterGray
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 8.dp)
            )

            LazyColumn(
                contentPadding = PaddingValues(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredRecipes.chunked(2)) { rowRecipes ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        rowRecipes.forEach { recipe ->
                            val isFavouriteLiveData = recipeViewModel.isFavourite(recipe.recipeId ?: 0, userId)
                            RecipeItem(
                                recipe = recipe,
                                navController = navController,
                                modifier = Modifier.weight(1f),
                                recipeViewModel = recipeViewModel,
                                userViewModel = userViewModel,
                                userId = userId,
                                isFavourite = isFavouriteLiveData,
                                onFavouriteClicked = {
                                    val recipeId = recipe.recipeId ?: 0
                                    val isFav = isFavouriteLiveData.value ?: false

                                    if (isFav) {
                                        recipeViewModel.deleteFavourite(Favourite(recipeId = recipeId, userId = userId))
                                    } else {
                                        recipeViewModel.addFavourite(Favourite(recipeId = recipeId, userId = userId))
                                    }
                                },
                                onDeleteClicked = {
                                    showDeleteDialog = true
                                    recipeToDelete = recipe
                                },
                                isUserRecipe = false
                            )
                        }
                        if (rowRecipes.size < 2) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            if (filteredRecipes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No recipes found.",
                        color = LighterGray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        if (showDeleteDialog) {
            showDialog(
                onConfirm = {
                    recipeToDelete?.let { recipe ->
                        recipeViewModel.deleteRecipe(recipe.recipeId ?: 0)
                    }
                    showDeleteDialog = false
                },
                onDismiss = { showDeleteDialog = false }
            )
        }
    }
}

@Composable
fun RecipeItem(
    recipe: Recipe,
    navController: NavHostController,
    modifier: Modifier = Modifier,
    recipeViewModel: RecipeViewModel,
    userViewModel: UserViewModel,
    userId: Long,
    isFavourite: LiveData<Boolean>,
    onFavouriteClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
    isUserRecipe: Boolean
) {
    val recipeId = recipe.recipeId ?: 0
    val isFavouriteValue by isFavourite.observeAsState(false) // Ensure initial state is false

    val isDarkTheme = isSystemInDarkTheme()

    var user by remember { mutableStateOf<User?>(null) }

    // Fetch user details
    LaunchedEffect(key1 = userViewModel, key2 = recipe.authorId) {
        user = userViewModel.getUserById(recipe.authorId)
    }

    Box(
        modifier = modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
            .clickable {
                navController.navigate("details/${recipe.recipeId}")
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
                    style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = if (recipe.name.length > 30) 8.dp else 8.dp),
                    textAlign = TextAlign.Start,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Display user profile picture and username
                user?.let {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 8.dp)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                ImageRequest.Builder(LocalContext.current)
                                    .data(data = it.image ?: R.drawable.profile)
                                    .apply(block = fun ImageRequest.Builder.() {
                                        placeholder(R.drawable.profile)
                                    }).build()
                            ),
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = it.username,
                            color = White,
                            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        )
                    }
                }

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
                    CompositionLocalProvider(LocalContentColor provides if (isFavouriteValue) Orange else White) {
                        IconButton(
                            onClick = onFavouriteClicked,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (isFavouriteValue) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = if (isFavouriteValue) "Unlike Recipe" else "Like Recipe",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    if (recipe.authorId == userId) {
                        IconButton(
                            onClick = onDeleteClicked,
                            modifier = Modifier.weight(1f) // Use weight to align it to the end
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Recipe"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun showDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Recipe") },
        text = { Text("Are you sure you want to delete this recipe?") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Delete", color = Color.White)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text("Cancel", color = Color.White)
            }
        }
    )
}
