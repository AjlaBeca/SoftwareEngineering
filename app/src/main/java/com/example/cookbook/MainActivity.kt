
package com.example.cookbook

import HomeScreen
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.cookbook.data.models.Recipe
import com.example.cookbook.data.repositories.RecipeRepository
import com.example.cookbook.data.viewmodels.RecipeViewModel
import com.example.cookbook.data.viewmodels.UserViewModel
import com.example.cookbook.ui.screens.*
import com.example.cookbook.ui.theme.CookBookTheme
import com.example.cookbook.utils.SharedPreferencesUtil

class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"
    private val userViewModel: UserViewModel by viewModels { UserViewModel.UserViewModelFactory(application) }
    private val recipeViewModel: RecipeViewModel by viewModels {
        RecipeViewModel.RecipeViewModelFactory(RecipeRepository(application))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: UserViewModel initialized successfully")
        setContent {
            CookBookApp(userViewModel, recipeViewModel)
        }
    }
}

@Composable
fun CookBookApp(userViewModel: UserViewModel, recipeViewModel: RecipeViewModel) {
    val navController = rememberNavController()
    val recipeList by recipeViewModel.readAllData.observeAsState(initial = emptyList())

    CookBookTheme {
        val isLoggedIn = remember { mutableStateOf(SharedPreferencesUtil.isLoggedIn(navController.context)) }

        // Restore the current user ID if logged in
        LaunchedEffect(Unit) {
            if (isLoggedIn.value) {
                userViewModel.currentUserId.value = SharedPreferencesUtil.getUserId(navController.context).takeIf { it != -1L }
            }
        }

        // Launch effect to handle navigation based on login status
        LaunchedEffect(isLoggedIn.value) {
            if (!isLoggedIn.value) {
                navController.navigate("login") {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }

        MyApp(navController, userViewModel, recipeViewModel, recipeList, isLoggedIn.value)
    }
}

@Composable
fun MyApp(navController: NavHostController, userViewModel: UserViewModel, recipeViewModel: RecipeViewModel, recipeList: List<Recipe>, isLoggedIn: Boolean) {
    val startDestination = if (isLoggedIn) "list" else "login"

    Scaffold(
        bottomBar = {
            val currentDestination = navController.currentBackStackEntryAsState().value?.destination
            if (currentDestination?.route in listOf("home", "list", "profile")) {
                BottomNavigationBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(navController = navController, startDestination = startDestination, Modifier.padding(innerPadding)) {
            composable("login") { LoginScreen(navController, userViewModel) }
            composable("signup") { SignUpScreen(navController, userViewModel) }
            composable("home") { HomeScreen(navController, userViewModel) }
            composable("list") {
                ListScreen(navController, "", recipeViewModel) // Checking "list" route
            }
            composable("listScreen/{category}") { backStackEntry ->
                val category = backStackEntry.arguments?.getString("category")
                if (category != null) {
                    ListScreen(navController = navController, category = category, recipeViewModel)
                }
            }
            composable("profile") { ProfileScreen(navController, userViewModel, recipeViewModel) }
            composable("add_screen") { AddScreen(navController, recipeViewModel, userViewModel, userViewModel.currentUserId.value) }
            composable("recipe_detail_screen/{recipeId}") { backStackEntry ->
                val recipeId = backStackEntry.arguments?.getString("recipeId")?.toIntOrNull()
                Log.d("Navigation", "Recipe ID: $recipeId")
                val recipe = recipeList.find { it.recipeId == recipeId }
                recipe?.let {
                    RecipeDetailScreen(recipe, userViewModel)
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = currentRoute(navController) == "home",
            onClick = { navigateTo(navController, "home") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "List") },
            label = { Text("List") },
            selected = currentRoute(navController) == "list",
            onClick = { navigateTo(navController, "list") }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
            label = { Text("Profile") },
            selected = currentRoute(navController) == "profile",
            onClick = { navigateTo(navController, "profile") }
        )
    }
}

fun navigateTo(navController: NavHostController, route: String) {
    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun currentRoute(navController: NavHostController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    CookBookTheme {
        Text(text = "Hello Android!")
    }
}
