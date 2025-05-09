package com.example.cookbook.ui.screens

import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.ContentScale
import com.example.cookbook.ui.theme.*
import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.*
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import coil.request.ImageRequest
import android.content.res.Resources
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.lifecycle.LiveData
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.cookbook.R
import com.example.cookbook.data.models.Favourite
import com.example.cookbook.data.models.Recipe
import com.example.cookbook.data.models.RecipeLikeCount
import com.example.cookbook.data.models.User
import com.example.cookbook.data.viewmodels.RecipeViewModel
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

    // Observe either filtered or all recipes
    val recipesSource: LiveData<List<Recipe>> =
        categoryId?.let { recipeViewModel.getRecipesByCategory(it) }
            ?: recipeViewModel.getAllRecipes()
    val allRecipes by recipesSource.observeAsState(emptyList())

    var searchText by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var recipeToDelete by remember { mutableStateOf<Recipe?>(null) }

    val recipeLikeCounts by recipeViewModel.recipeLikeCounts.observeAsState(emptyList())

    // Filter out user's own and match search
    val filteredRecipes = allRecipes
        .filter { it.authorId != userId }
        .filter { it.name.contains(searchText, ignoreCase = true) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Back arrow if in category
        if (category != null) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
        }

        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            label = { Text("Search") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        LazyColumn(
            contentPadding = PaddingValues(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(filteredRecipes.chunked(2)) { rowRecipes ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    rowRecipes.forEach { recipe: Recipe ->
                        val isFavLD = recipeViewModel.isFavourite(recipe.recipeId ?: 0, userId)
                        RecipeItem(
                            recipe = recipe,
                            navController = navController,
                            modifier = Modifier.weight(1f),
                            recipeViewModel = recipeViewModel,
                            userViewModel = userViewModel,
                            userId = userId,
                            isFavourite = isFavLD,
                            onFavouriteClicked = {
                                val rid = recipe.recipeId ?: 0
                                val fav = Favourite(recipeId = rid, userId = userId)
                                if (isFavLD.value == true)
                                    recipeViewModel.deleteFavourite(fav)
                                else
                                    recipeViewModel.addFavourite(fav)
                            },
                            onDeleteClicked = {
                                showDeleteDialog = true
                                recipeToDelete = recipe
                            },
                            isUserRecipe = false,
                            recipeLikeCounts = recipeLikeCounts
                        )
                    }
                    if (rowRecipes.size < 2) Spacer(Modifier.weight(1f))
                }
            }
        }

        if (filteredRecipes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No recipes found.", textAlign = TextAlign.Center)
            }
        }
    }

    if (showDeleteDialog) {
        showDialog(
            onConfirm = {
                recipeToDelete?.recipeId?.let { recipeViewModel.deleteRecipe(it) }
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
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
    isUserRecipe: Boolean,
    recipeLikeCounts: List<RecipeLikeCount>
) {
    val recipeId = recipe.recipeId ?: 0
    val isFavouriteValue by isFavourite.observeAsState(false)
    var user by remember { mutableStateOf<User?>(null) }

    // Fetch user details
    LaunchedEffect(key1 = userViewModel, key2 = recipe.authorId) {
        user = userViewModel.getUserById(recipe.authorId)
    }

    val likeCount = recipeLikeCounts.find { it.recipeId == recipeId }?.likeCount ?: 0

    Box(
        modifier = modifier
            .padding(vertical = 4.dp)
            .fillMaxWidth()
            .clickable {
                navController.navigate("details/${recipe.recipeId}")
            }
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape = RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .border(
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(10.dp)
                ),
        ) {
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer)
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
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = if (recipe.name.length > 15) 4.dp else 24.dp
                        ),
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
                            .padding(bottom = 4.dp)
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
                            color = MaterialTheme.colorScheme.secondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("Time: ")
                        }
                        append(recipe.time)
                    },
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 8.dp),
                    textAlign = TextAlign.Start
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 8.dp)
                ) {
                    Text(
                        text = "Complexity: ",
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = recipe.complexity,
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 26.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = if (isUserRecipe) Arrangement.SpaceBetween else Arrangement.Center
                ) {
                    CompositionLocalProvider(LocalContentColor provides if (isFavouriteValue) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically

                        ) {
                            IconButton(
                                onClick = onFavouriteClicked,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFavouriteValue) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = if (isFavouriteValue) "Unlike Recipe" else "Like Recipe"
                                )
                            }
                            Text(
                                text = likeCount.toString(),
                                color = MaterialTheme.colorScheme.secondary,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }

                    if (isUserRecipe) {
                        IconButton(
                            onClick = onDeleteClicked,
                            modifier = Modifier
                                .size(24.dp)
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
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete", color = White)
                    }
                },
                dismissButton = {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Text("Cancel", color = White)
                    }
                }
            )
        }
