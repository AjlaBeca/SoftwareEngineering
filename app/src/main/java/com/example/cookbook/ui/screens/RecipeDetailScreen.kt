package com.example.cookbook.ui.screens
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import com.example.cookbook.data.models.Recipe
import com.example.cookbook.data.models.User
import com.example.cookbook.data.viewmodels.UserViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(recipe: Recipe, userViewModel: UserViewModel) {
    val navController = rememberNavController()
    var user by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(key1 = userViewModel, key2 = recipe.authorId) {
        val fetchedUser = userViewModel.getUserById(recipe.authorId.toLong()) // Convert to Long
        user = fetchedUser
        println("Fetched user: $fetchedUser") // Print fetched user data
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
            user?.let { UserDetail(recipe = recipe, user = it, padding = padding) } // Pass padding to UserDetail
        }
    )
}
@Composable
fun UserDetail(recipe: Recipe, user: User, padding: PaddingValues) { // Receive padding parameter
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // Add vertical scroll
    ) {
        recipe.imagePath?.let { imageUrl ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Image(
                    painter = rememberImagePainter(data = imageUrl),
                    contentDescription = "Recipe Image",
                    modifier = Modifier
                        .fillMaxWidth() // Take full width of the parent
                        .clip(RoundedCornerShape(8.dp))
                        .height(400.dp),

                    contentScale = ContentScale.FillWidth
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 16.dp) // Increase horizontal padding
        ) {
            Text(text = "Recipe by ${user.username}", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(8.dp))
            // Display recipe details
            Text(text = recipe.name, style = MaterialTheme.typography.displayMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Time:",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelLarge
            )
            Text(text = recipe.time, style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Complexity:",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelLarge
            )
            Text(text = recipe.complexity, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Servings:",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelLarge
            )
            Text(text = "${recipe.servings}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Ingredients:",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelLarge
            )
            Text(text = recipe.ingredients, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Instructions:",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelLarge
            )
            Text(text = recipe.instructions, style = MaterialTheme.typography.bodySmall)
        }
    }
}