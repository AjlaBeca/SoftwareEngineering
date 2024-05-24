package com.example.cookbook

import ListScreen
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
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
import com.example.cookbook.ui.screens.AddScreen
import com.example.cookbook.ui.screens.HomeScreen
import com.example.cookbook.ui.screens.LoginScreen
import com.example.cookbook.ui.screens.ProfileScreen
import com.example.cookbook.ui.screens.SignUpScreen
import com.example.cookbook.ui.theme.CookBookTheme

class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"
    private val userViewModel: UserViewModel by viewModels { UserViewModel.UserViewModelFactory(this.application) }
    private val recipeViewModel: RecipeViewModel by viewModels {
        RecipeViewModel.RecipeViewModelFactory(RecipeRepository(this.application))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: UserViewModel initialized successfully")
        setContent {
            val navController = rememberNavController()
            CookBookTheme {
                val recipeList by recipeViewModel.readAllData.observeAsState(initial = emptyList())
                MyApp(navController, userViewModel, recipeViewModel, recipeList)
            }
        }
    }
}

@Composable
fun MyApp(navController: NavHostController, userViewModel: UserViewModel, recipeViewModel: RecipeViewModel, recipeList: List<Recipe>) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            if (currentDestination?.route in listOf("home", "list", "profile")) {
                BottomNavigationBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(navController = navController, startDestination = "login", Modifier.padding(innerPadding)) {
            composable("login") { LoginScreen(navController, userViewModel) }
            composable("signup") { SignUpScreen(navController, userViewModel) }
            composable("home") { HomeScreen(navController) }
            composable("list") { ListScreen(navController, recipeList) }
            composable("profile") { ProfileScreen(navController) }
            composable("add_screen") { AddScreen(navController, recipeViewModel) }
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
            icon = { Icon(Icons.Default.List, contentDescription = "List") },
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
