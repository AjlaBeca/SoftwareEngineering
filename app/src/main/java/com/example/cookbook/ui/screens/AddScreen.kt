@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.cookbook.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.*
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Scale
import coil.size.Size
import com.example.cookbook.data.viewmodels.RecipeViewModel
import com.example.cookbook.data.viewmodels.UserViewModel
import kotlinx.coroutines.launch
// AddScreen.kt (fixed)
@Composable
fun AddScreen(
    navController: NavHostController,
    recipeViewModel: RecipeViewModel,
    userViewModel: UserViewModel
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    val currentUser by userViewModel.currentUser.observeAsState()

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    val imageUri = recipeViewModel.recipeImageUri

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                recipeViewModel.onImageUriChange(it)
                saveImageUriToPreferences(context, it)
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Recipe") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                OutlinedTextField(
                    value = recipeViewModel.recipeName,
                    onValueChange = recipeViewModel::onRecipeNameChange,
                    label = { Text("Recipe Name") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                OutlinedTextField(
                    value = recipeViewModel.recipeInstructions,
                    onValueChange = recipeViewModel::onRecipeInstructionsChange,
                    label = { Text("Instructions") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                OutlinedTextField(
                    value = recipeViewModel.recipeIngredients,
                    onValueChange = recipeViewModel::onRecipeIngredientsChange,
                    label = { Text("Ingredients") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                OutlinedTextField(
                    value = recipeViewModel.recipeTime,
                    onValueChange = recipeViewModel::onRecipeTimeChange,
                    label = { Text("Time") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                DropdownMenuComplexity(
                    selected = recipeViewModel.recipeComplexity,
                    onSelect = recipeViewModel::onRecipeComplexityChange
                )

                DropdownMenuCategory(
                    selected = recipeViewModel.recipeCategoryId.toString(),
                    onSelect = { recipeViewModel.onCategoryIdChange(it.toInt()) }
                )

                OutlinedTextField(
                    value = recipeViewModel.recipeServings.toString(),
                    onValueChange = {
                        recipeViewModel.onRecipeServingsChange(it.toIntOrNull() ?: 1)
                    },
                    label = { Text("Servings") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    )
                )

                OutlinedButton(
                    onClick = { launcher.launch("image/*") }
                ) {
                    Text("Select Image")
                }

                imageUri?.let {
                    Image(
                        painter = rememberAsyncImagePainter(it),
                        contentDescription = null,
                        modifier = Modifier.size(100.dp)
                    )
                }

                Button(
                    onClick = {
                        val userId = currentUser?.userId
                        if (userId == null) {
                            errorMessage = "You must be signed in"
                            return@Button
                        }

                        if (!recipeViewModel.isValidRecipe()) {
                            errorMessage = "Please fill in all required fields"
                            return@Button
                        }

                        isSubmitting = true
                        coroutineScope.launch {
                            recipeViewModel.addRecipe(authorId = userId)
                            isSubmitting = false
                            navController.popBackStack()
                        }
                    },
                    enabled = !isSubmitting
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Text("Add Recipe")
                    }
                }
            }
        }
    }
}

@Composable
fun DropdownMenuComplexity(selected: String, onSelect: (String) -> Unit) {
    val options = listOf("Beginner", "Intermediate", "Advanced", "Expert")
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            label = { Text("Recipe Complexity") },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach {
                DropdownMenuItem(
                    text = { Text(it) },
                    onClick = {
                        onSelect(it)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun DropdownMenuCategory(selected: String, onSelect: (String) -> Unit) {
    val options = listOf("Breakfast", "Lunch", "Dinner", "Dessert")
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            label = { Text("Recipe Category") },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach {
                DropdownMenuItem(
                    text = { Text(it) },
                    onClick = {
                        onSelect(it)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun saveImageUriToPreferences(context: Context, uri: Uri?) {
    val sharedPref = context.getSharedPreferences("image_pref", Context.MODE_PRIVATE)
    sharedPref.edit().putString("image_uri", uri?.toString()).apply()
    Log.d("SharedPreferences", "Saved URI: ${uri?.toString()}")
}
