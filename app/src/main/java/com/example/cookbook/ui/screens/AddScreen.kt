package com.example.cookbook.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.cookbook.data.viewmodels.RecipeViewModel

@Composable
fun AddScreen(navController: NavHostController, recipeViewModel: RecipeViewModel) {
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var name by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }
    var ingredients by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var complexity by remember { mutableStateOf("Beginner") }
    var servings by remember { mutableStateOf(1) }
    var category by remember { mutableStateOf("Breakfast") }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            imageUri = uri
            recipeViewModel.onImageUriChange(uri)
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
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

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = instructions,
            onValueChange = {
                instructions = it
                recipeViewModel.onRecipeInstructionsChange(it)
            },
            label = { Text("Recipe Instructions") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = ingredients,
            onValueChange = {
                ingredients = it
                recipeViewModel.onRecipeIngredientsChange(it)
            },
            label = { Text("Recipe Ingredients") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

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

        Spacer(modifier = Modifier.height(16.dp))

        DropdownMenuComplexity(complexity) {
            complexity = it
            recipeViewModel.onRecipeComplexityChange(it)
        }

        Spacer(modifier = Modifier.height(16.dp))

        DropdownMenuCategory(category) {
            category = it
            recipeViewModel.onRecipeCategoryIdChange(categoryToId(it))
        }

        Spacer(modifier = Modifier.height(16.dp))

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

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { launcher.launch("image/*") }) {
            Text("Select Image")
        }

        Spacer(modifier = Modifier.height(16.dp))

        imageUri?.let {
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .padding(4.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                recipeViewModel.addRecipe()
                navController.popBackStack()
            },
            enabled = recipeViewModel.isValidRecipe()
        ) {
            Text("Add Recipe")
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
                        onComplexitySelected(complexity)
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
                        onCategorySelected(category)
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

@Preview
@Composable
fun PreviewAddScreen() {
    AddScreen(navController = rememberNavController(), recipeViewModel = viewModel())
}
