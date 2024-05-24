import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.cookbook.data.models.Recipe

@Composable
fun ListScreen(navController: NavHostController, recipes: List<Recipe>) {
    Column {
        Button(
            onClick = { navController.navigate("add_screen") },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Add Recipe")
        }
        LazyColumn {
            items(recipes) { recipe ->
                RecipeItem(recipe = recipe)
            }
        }
    }
}

@Composable
fun RecipeItem(recipe: Recipe) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Recipe Image
        recipe.imagePath?.let { imagePath ->
            val painter = rememberAsyncImagePainter(imagePath)
            Image(
                painter = painter,
                contentDescription = null, // Provide proper content description
                modifier = Modifier.size(64.dp),
                contentScale = ContentScale.Crop
            )
        }
        Column(
            modifier = Modifier
                .padding(start = 16.dp)
                .weight(1f)
        ) {
            Text(text = recipe.name)
        }
    }
}
