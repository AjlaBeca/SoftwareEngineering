package com.example.cookbook.ui.screens

import RecipeViewModel
import android.content.Context
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.cookbook.R
import com.example.cookbook.data.models.Favourite
import com.example.cookbook.data.models.Recipe
import com.example.cookbook.data.viewmodels.UserViewModel
import com.example.cookbook.ui.theme.Gray
import com.example.cookbook.ui.theme.LighterGray
import com.example.cookbook.ui.theme.Orange
import com.example.cookbook.ui.theme.White
import com.example.cookbook.utils.SharedPreferencesUtil
import kotlinx.coroutines.launch


@Composable
fun ProfileScreen(
    navController: NavHostController,
    userViewModel: UserViewModel,
    recipeViewModel: RecipeViewModel,
    userId: Long
) {
    val coroutineScope = rememberCoroutineScope()
    val currentUserId by userViewModel.currentUserId.observeAsState()
    val userProfile by userViewModel.currentUser.observeAsState()
    val recipeLikeCounts by recipeViewModel.recipeLikeCounts.observeAsState(listOf())

    if (currentUserId == null) {
        Text("Please log in to view your profile.")
        return
    }

    val context = LocalContext.current
    val imageUri by remember { mutableStateOf(loadImageUriFromPreferences(context, userId)) }

    var searchText by remember { mutableStateOf("") }
    var showUserRecipes by remember { mutableStateOf(true) }

    val userRecipes by recipeViewModel.getRecipesByUser(currentUserId ?: -1).observeAsState(listOf())
    val favouriteRecipes by recipeViewModel.getUserFavourites(currentUserId ?: -1).observeAsState(listOf())

    val filteredRecipes = if (showUserRecipes) {
        userRecipes.filter { recipe -> searchText.isEmpty() || recipe.name.contains(searchText, ignoreCase = true) }
    } else {
        favouriteRecipes.filter { recipe -> searchText.isEmpty() || recipe.name.contains(searchText, ignoreCase = true) }
    }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var recipeToDelete by remember { mutableStateOf<Recipe?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .clickable { navController.navigate("editProfile") }
            ) {
                val painter = imageUri?.let { rememberAsyncImagePainter(model = it) }
                    ?: rememberAsyncImagePainter(model = R.drawable.profile)

                Image(
                    painter = painter,
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                IconButton(
                    onClick = { navController.navigate("editProfile") },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile",
                        tint = White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
            Text(
                text = userProfile?.username ?: "USERNAME",
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = { showUserRecipes = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (showUserRecipes) MaterialTheme.colorScheme.primary else Color.Transparent,
                        contentColor = if (showUserRecipes) White else MaterialTheme.colorScheme.tertiary
                    ),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .padding(start = 8.dp, end = 4.dp)
                        .clip(RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
                        .border(
                            width = 1.dp,
                            color = if (showUserRecipes) Color.Transparent else MaterialTheme.colorScheme.primaryContainer,
                            shape = MaterialTheme.shapes.medium
                        )
                ) {
                    Text(
                        text = "My Recipes",
                        color = if (showUserRecipes) White else MaterialTheme.colorScheme.tertiary,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                    )
                }
                Spacer(modifier = Modifier.width(2.dp))
                Button(
                    onClick = { showUserRecipes = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!showUserRecipes) MaterialTheme.colorScheme.primary else Color.Transparent,
                        contentColor = if (!showUserRecipes) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary
                    ),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .padding(start = 4.dp, end = 8.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .border(
                            width = 1.dp,
                            color = if (!showUserRecipes) Color.Transparent else MaterialTheme.colorScheme.primaryContainer,
                            shape = MaterialTheme.shapes.medium
                        )
                ) {
                    Text(
                        text = "Favourites",
                        color = if (!showUserRecipes) White else MaterialTheme.colorScheme.tertiary,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (showUserRecipes) {
                Spacer(modifier = Modifier.height(8.dp))
                AddRecipeButton(navController = navController, modifier = Modifier.fillMaxWidth().height(62.dp))
            }

            if (filteredRecipes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (showUserRecipes) "No recipes found." else "No favourites found.",
                        color = MaterialTheme.colorScheme.tertiary,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
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
                                    isUserRecipe = recipe.authorId == userId,
                                    recipeLikeCounts = recipeLikeCounts
                                )
                            }
                            if (rowRecipes.size < 2) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        IconButton(
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
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = "Logout",
                tint = MaterialTheme.colorScheme.error
            )
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
fun AddRecipeButton(navController: NavHostController, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(8.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable { navController.navigate("addRecipe") },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add Recipe",
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(48.dp)
        )
    }
}

private fun loadImageUriFromPreferences(context: Context, userId: Long): Uri? {
    val sharedPref = context.getSharedPreferences("image_pref_$userId", Context.MODE_PRIVATE)
    val imageUriString = sharedPref.getString("image_uri", null)
    return imageUriString?.let { Uri.parse(it) }
}
