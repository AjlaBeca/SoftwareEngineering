package com.example.cookbook.ui.screens

import RecipeViewModel
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.example.cookbook.R
import com.example.cookbook.data.models.Favourite
import com.example.cookbook.data.models.Recipe
import com.example.cookbook.data.viewmodels.UserViewModel
import com.example.cookbook.ui.theme.Gray
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

    // Check if user is logged in before displaying the profile
    if (currentUserId == null) {
        Text("Please log in to view your profile.")
        return
    }

    val context = LocalContext.current
    val imageUri by remember { mutableStateOf(loadImageUriFromPreferences(context, userId)) }

    var searchText by remember { mutableStateOf("") }

    val userRecipes by recipeViewModel.getRecipesByUser(currentUserId ?: -1)
        .observeAsState(listOf())
    val filteredRecipes = userRecipes.filter { recipe ->
        searchText.isEmpty() || recipe.name.contains(searchText, ignoreCase = true)
    }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var recipeToDelete by remember { mutableStateOf<Recipe?>(null) }

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
                .background(Color.Gray)
                .clickable {
                    navController.navigate("editProfile")
                }
        ) {
            val painter = imageUri?.let { rememberImagePainter(data = it) }
                ?: rememberImagePainter(data = R.drawable.profile)

            Image(
                painter = painter,
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            IconButton(
                onClick = {
                    navController.navigate("editProfile")
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Profile",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Text(
            text = userProfile?.username ?: "USERNAME",
            color = White,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.displayMedium,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            contentPadding = PaddingValues(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(filteredRecipes.chunked(2)) { rowRecipes ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    rowRecipes.forEachIndexed { index, recipe ->
                        RecipeItem(
                            recipe = recipe,
                            navController = navController,
                            modifier = Modifier.weight(1f),
                            recipeViewModel = recipeViewModel,
                            userId = userId,
                            isFavourite = recipeViewModel.isFavourite(recipe.recipeId ?: 0, userId),
                            onFavouriteClicked = {
                                val recipeId = recipe.recipeId ?: 0
                                val isFav = recipeViewModel.isFavourite(recipeId, userId).value ?: false

                                if (isFav) {
                                    recipeViewModel.deleteFavourite(
                                        Favourite(
                                            recipeId = recipeId,
                                            userId = userId
                                        )
                                    )
                                } else {
                                    recipeViewModel.addFavourite(
                                        Favourite(
                                            recipeId = recipeId,
                                            userId = userId
                                        )
                                    )
                                }
                            },
                            onDeleteClicked = {
                                showDeleteDialog = true
                                recipeToDelete = recipe
                            }
                        )
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

    if (showDeleteDialog) {
        showDialog(
            onConfirm = {
                recipeToDelete?.let { recipe ->
                    recipeViewModel.deleteRecipe(recipe.recipeId ?: 0)
                }
                showDeleteDialog = false
            },
            onDismiss = {
                showDeleteDialog = false
            }
        )
    }
}

private fun loadImageUriFromPreferences(context: Context, userId: Long): Uri? {
    val sharedPref = context.getSharedPreferences("image_pref_$userId", Context.MODE_PRIVATE)
    val imageUriString = sharedPref.getString("image_uri", null)
    return imageUriString?.let { Uri.parse(it) }
}
