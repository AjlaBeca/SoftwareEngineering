package com.example.cookbook.ui.screens
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.cookbook.R
import com.example.cookbook.data.models.Favourite
import com.example.cookbook.data.models.User
import com.example.cookbook.data.viewmodels.RecipeViewModel
import com.example.cookbook.data.viewmodels.UserViewModel

@Composable
fun UserScreen(
    navController: NavHostController,
    userId: Long,
    userViewModel: UserViewModel,
    recipeViewModel: RecipeViewModel
) {
    val currentUserId by userViewModel.currentUserId.observeAsState()
    val recipeLikeCounts by recipeViewModel.recipeLikeCounts.observeAsState(listOf())

    if (currentUserId == userId) {
        LaunchedEffect(Unit) {
            navController.navigate("profile") {
                popUpTo(navController.graph.startDestinationId)
                launchSingleTop = true
            }
        }
        return
    }

    var user by remember { mutableStateOf<User?>(null) }
    val userRecipes by recipeViewModel.getRecipesByUser(userId).observeAsState(listOf())

    LaunchedEffect(key1 = userViewModel, key2 = userId) {
        user = userViewModel.getUserById(userId)
    }

    LaunchedEffect(key1 = userRecipes) {
        currentUserId?.let { currentUserIdNonNull ->
            userRecipes.forEach { recipe ->
                recipeViewModel.fetchInitialFavouriteStatus(recipe.recipeId ?: 0, currentUserIdNonNull)
            }
        }
    }

    user?.let { user ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                ) {
                    val painter = rememberAsyncImagePainter(model = user.image ?: R.drawable.profile)

                    Image(
                        painter = painter,
                        contentDescription = "Profile Picture",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Text(
                    text = user.username,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.displayMedium,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (userRecipes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 30.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No recipes found.",
                            color = MaterialTheme.colorScheme.secondary,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(userRecipes.chunked(2)) { rowRecipes ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                rowRecipes.forEach { recipe ->
                                    currentUserId?.let { currentUserIdNonNull ->
                                        val isFavouriteLiveData = recipeViewModel.isFavourite(recipe.recipeId ?: 0, currentUserIdNonNull)
                                        RecipeItem(
                                            recipe = recipe,
                                            navController = navController,
                                            modifier = Modifier.weight(1f),
                                            recipeViewModel = recipeViewModel,
                                            userViewModel = userViewModel,
                                            userId = currentUserIdNonNull,
                                            isFavourite = isFavouriteLiveData,
                                            onFavouriteClicked = {
                                                val recipeId = recipe.recipeId ?: 0
                                                val isFav = isFavouriteLiveData.value ?: false

                                                if (isFav) {
                                                    recipeViewModel.deleteFavourite(Favourite(recipeId = recipeId, userId = currentUserIdNonNull))
                                                } else {
                                                    recipeViewModel.addFavourite(Favourite(recipeId = recipeId, userId = currentUserIdNonNull))
                                                }
                                            },
                                            onDeleteClicked = {},
                                            isUserRecipe = false,
                                            recipeLikeCounts = recipeLikeCounts
                                        )
                                    }
                                }
                                if (rowRecipes.size < 2) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}