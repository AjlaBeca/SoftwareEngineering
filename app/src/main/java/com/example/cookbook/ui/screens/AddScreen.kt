package com.example.cookbook.ui.screens

import RecipeViewModel
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Scale
import coil.size.Size
import com.example.cookbook.data.viewmodels.UserViewModel
import com.example.cookbook.ui.theme.*

@Composable
fun AddScreen(navController: NavHostController, recipeViewModel: RecipeViewModel, userViewModel: UserViewModel, currentUserId: Long?) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }
    var ingredients by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var complexity by remember { mutableStateOf("Beginner") }
    var servings by remember { mutableIntStateOf(1) }
    var category by remember { mutableStateOf("Breakfast") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    if (complexity.isEmpty()) {
        complexity = "Beginner"
    }
    if (category.isEmpty()) {
        category = "Breakfast"
    }

    val currentUserId by userViewModel.currentUserId.observeAsState()
    Log.d("AddScreen", "Current user ID: $currentUserId")

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            imageUri = uri
            saveImageUriToPreferences(context, uri)
            recipeViewModel.onImageUriChange(uri)
        }
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            recipeViewModel.onRecipeNameChange(it)
                        },
                        label = { Text("Recipe Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = instructions,
                        onValueChange = {
                            instructions = it
                            recipeViewModel.onRecipeInstructionsChange(it)
                        },
                        label = { Text("Recipe Instructions") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = ingredients,
                        onValueChange = {
                            ingredients = it
                            recipeViewModel.onRecipeIngredientsChange(it)
                        },
                        label = { Text("Recipe Ingredients") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = time,
                        onValueChange = {
                            time = it
                            recipeViewModel.onRecipeTimeChange(it)
                        },
                        label = { Text("Recipe Time (e.g., 30 min, 1 hour)") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(onNext = { FocusDirection.Down })
                    )

                    DropdownMenuComplexity(complexity) {
                        complexity = it
                        recipeViewModel.onRecipeComplexityChange(it)
                    }

                    DropdownMenuCategory(category) {
                        category = it
                        recipeViewModel.onRecipeCategoryIdChange(categoryToId(it))
                    }

                    OutlinedTextField(
                        value = servings.toString(),
                        onValueChange = {
                            servings = it.toIntOrNull() ?: 1
                            recipeViewModel.onRecipeServingsChange(it.toIntOrNull() ?: 1)
                        },
                        label = { Text("Servings") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        )
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { launcher.launch("image/*") },
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .padding(horizontal = 4.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = White),
                ) {
                    Text("Select Image")
                }

                // Image preview
                imageUri?.let {
                    val request = ImageRequest.Builder(context)
                        .data(it)
                        .bitmapConfig(Bitmap.Config.ARGB_8888)
                        .size(Size.ORIGINAL)
                        .scale(Scale.FILL)
                        .precision(Precision.EXACT)
                        .build()

                    Image(
                        painter = rememberAsyncImagePainter(request),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(4.dp)
                            .size(100.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Button to add recipe
                Button(
                    onClick = {
                        val recipeId = recipeViewModel.addRecipe(currentUserId?.toLong() ?: return@Button)
                        navController.popBackStack()
                    },
                    enabled = recipeViewModel.isValidRecipe(),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(containerColor = Orange, contentColor = White),
                ) {
                    Text("Add Recipe")
                }
            }
        }
    }
}
@Composable
fun DropdownMenuComplexity(selectedComplexity: String, onComplexitySelected: (String) -> Unit) {
    val complexityOptions = listOf("Beginner", "Intermediate", "Advanced", "Expert")
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedTextField(
            value = selectedComplexity,
            onValueChange = { /* No-op */ },
            label = { Text("Recipe Complexity") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                }
            }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            complexityOptions.forEach { complexity ->
                DropdownMenuItem(
                    text = { Text(complexity) },
                    onClick = {
                        onComplexitySelected(complexity) // Update the selected complexity
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun DropdownMenuCategory(selectedCategory: String, onCategorySelected: (String) -> Unit) {
    val categoryOptions = listOf("Breakfast", "Lunch", "Dinner", "Dessert")
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedTextField(
            value = selectedCategory,
            onValueChange = { /* No-op */ },
            label = { Text("Recipe Category") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                }
            }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categoryOptions.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category) },
                    onClick = {
                        onCategorySelected(category) // Update the selected category
                        expanded = false
                    }
                )
            }
        }
    }
}

fun categoryToId(category: String): Int {
    return when (category) {
        "Breakfast" -> 1
        "Lunch" -> 2
        "Dinner" -> 3
        "Dessert" -> 4
        else -> 1
    }
}

private fun saveImageUriToPreferences(context: Context, uri: Uri?) {
    val sharedPref = context.getSharedPreferences("image_pref", Context.MODE_PRIVATE)
    with(sharedPref.edit()) {
        putString("image_uri", uri?.toString())
        apply()
    }
    Log.d("SharedPreferences", "Saved URI: ${uri?.toString()}")
}