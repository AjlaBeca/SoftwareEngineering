package com.example.cookbook.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.cookbook.R
import com.example.cookbook.data.models.Recipe
import com.example.cookbook.data.models.User
import com.example.cookbook.data.viewmodels.UserViewModel
import com.example.cookbook.ui.theme.White


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    navController: NavHostController,
    recipe: Recipe,
    userViewModel: UserViewModel
) {
    var user by remember { mutableStateOf<User?>(null) }
    val currentUserId by userViewModel.currentUserId.observeAsState()

    LaunchedEffect(key1 = userViewModel, key2 = recipe.authorId) {
        val fetchedUser = userViewModel.getUserById(recipe.authorId)
        user = fetchedUser
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = recipe.name) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        content = { padding ->
            val currentUser = user
            if (currentUser != null) {
                UserDetail(
                    recipe = recipe,
                    user = currentUser,
                    isCurrentUser = currentUserId == currentUser.userId,
                    padding = padding,
                    onUserClick = {
                        navController.navigate("user/${currentUser.userId}")
                    }
                )
            } else {
                // Nothing
            }
        }
    )
}

@Composable
fun UserDetail(
    recipe: Recipe,
    user: User,
    isCurrentUser: Boolean,
    padding: PaddingValues,
    onUserClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        recipe.imagePath?.let { imageUrl ->
            Image(
                painter = rememberAsyncImagePainter(model = imageUrl),
                contentDescription = "Recipe Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .padding(bottom = 16.dp),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .clickable(onClick = onUserClick)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            ImageRequest.Builder(LocalContext.current)
                                .data(data = user.image ?: R.drawable.profile)
                                .apply {
                                    placeholder(R.drawable.profile)
                                }.build()
                        ),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = user.username,
                        color = MaterialTheme.colorScheme.secondary,
                        style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    )
                    if (isCurrentUser) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "(You)",
                            color = MaterialTheme.colorScheme.primary,
                            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        )
                    }
                }

                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                DetailSection(title = "Time", detail = recipe.time)
                DetailSection(title = "Complexity", detail = recipe.complexity)
                DetailSection(title = "Servings", detail = "${recipe.servings}")
                DetailSection(title = "Ingredients", detail = recipe.ingredients)
                DetailSection(title = "Instructions", detail = recipe.instructions)
            }
        }
    }
}


@Composable
fun DetailSection(title: String, detail: String) {
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = detail,
        style = MaterialTheme.typography.bodyMedium
    )
}
