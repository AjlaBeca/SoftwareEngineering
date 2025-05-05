package com.example.cookbook

import RecipeViewModel
import android.content.Context
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.cookbook.data.models.Recipe
import com.example.cookbook.data.viewmodels.UserViewModel
import com.example.cookbook.ui.screens.*
import com.example.cookbook.ui.theme.CookBookTheme
import com.example.cookbook.utils.RoomToFirebaseMigrator
import com.example.cookbook.utils.SharedPreferencesUtil
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"
    private val userViewModel: UserViewModel by viewModels {
        UserViewModel.UserViewModelFactory(application)
    }
    private val recipeViewModel: RecipeViewModel by viewModels {
        RecipeViewModel.RecipeViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: UserViewModel initialized successfully")
        setContent {
            CookBookApp(userViewModel, recipeViewModel)
        }
    }
}
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Initialize Firebase
    FirebaseApp.initializeApp(this)

    // Check if migration is needed
    val sharedPrefs = getSharedPreferences("migration_prefs", Context.MODE_PRIVATE)
    val migrationCompleted = sharedPrefs.getBoolean("firebase_migration_completed", false)

    if (!migrationCompleted) {
        lifecycleScope.launch {
            val migrator = RoomToFirebaseMigrator(applicationContext)
            try {
                migrator.migrateAllData()
                // Mark migration as completed
                sharedPrefs.edit().putBoolean("firebase_migration_completed", true).apply()
            } catch (e: Exception) {
                Log.e(TAG, "Migration failed", e)
            }
        }
    }

    Log.d(TAG, "onCreate: UserViewModel initialized successfully")
    setContent {
        CookBookApp(userViewModel, recipeViewModel)
    }
}

@Composable
fun CookBookApp(userViewModel: UserViewModel, recipeViewModel: RecipeViewModel) {
    val navController = rememberNavController()
    val recipeList by recipeViewModel.readAllData.observeAsState(initial = emptyList())
    val isLoggedIn by userViewModel.isLoggedIn.observeAsState(initial = false)

    CookBookTheme {
        // Restore the current user ID if logged in
        LaunchedEffect(Unit) {
            if (isLoggedIn) {
                userViewModel.currentUserId.value =
                    SharedPreferencesUtil.getUserId(navController.context).takeIf { it != -1L }
            }
        }

        // Launch effect to handle navigation based on login status
        LaunchedEffect(isLoggedIn) {
            if (!isLoggedIn) {
                navController.navigate("login") {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }

        MyApp(navController, userViewModel, recipeViewModel, recipeList, isLoggedIn)
    }
}

@Composable
fun MyApp(
    navController: NavHostController,
    userViewModel: UserViewModel,
    recipeViewModel: RecipeViewModel,
    recipeList: List<Recipe>,
    isLoggedIn: Boolean
) {
    val startDestination = if (isLoggedIn) "list" else "login"

    Scaffold(
        bottomBar = {
            val currentDestination =
                navController.currentBackStackEntryAsState().value?.destination
            if (currentDestination?.route in listOf("home", "list", "profile")) {
                BottomNavigationBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            Modifier.padding(innerPadding)
        ) {
            composable("login") { LoginScreen(navController, userViewModel) }
            composable("signup") { SignUpScreen(navController, userViewModel) }
            composable("home") { HomeScreen(navController, userViewModel) }
            composable("editProfile") {
                EditProfileScreen(
                    navController,
                    userViewModel,
                    userViewModel.currentUserId.value ?: -1L
                )
            }
            composable("list") {
                ListScreen(
                    navController,
                    null,
                    recipeViewModel,
                    userViewModel,
                    userViewModel.currentUserId.value ?: -1L
                )
            }
            composable("listScreen/{category}") { backStackEntry ->
                val category = backStackEntry.arguments?.getString("category")
                if (category != null) {
                    ListScreen(
                        navController = navController,
                        category = category,
                        recipeViewModel,
                        userViewModel,
                        userViewModel.currentUserId.value ?: -1L
                    )
                }
            }
            composable("profile") {
                ProfileScreen(
                    navController,
                    userViewModel,
                    recipeViewModel,
                    userViewModel.currentUserId.value ?: -1L
                )
            }
            composable("addRecipe") {
                AddScreen(
                    navController,
                    recipeViewModel,
                    userViewModel,
                    userViewModel.currentUserId.value ?: -1L
                )
            }
            composable("user/{userId}") { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId")?.toLong() ?: 0L
                UserScreen(navController, userId, userViewModel, recipeViewModel)
            }
            composable("profile/{userId}") { backStackEntry ->
                val userId =
                    backStackEntry.arguments?.getString("userId")?.toLongOrNull() ?: -1L
                ProfileScreen(navController, userViewModel, recipeViewModel, userId)
            }
            composable(
                route = "details/{recipeId}",
                arguments = listOf(navArgument("recipeId") { type = NavType.IntType })
            ) { backStackEntry ->
                val recipeId = backStackEntry.arguments?.getInt("recipeId")
                Log.d("Navigation", "Recipe ID: $recipeId")
                val recipe = recipeList.find { it.recipeId == recipeId }
                recipe?.let {
                    RecipeDetailScreen(navController, recipe, userViewModel)
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    Surface(
        color = Color.Transparent,
        contentColor = Color.Transparent
    ) {
        NavigationBar(containerColor = Color.Transparent) {

            val secondaryColor =
                MaterialTheme.colorScheme.secondary

            NavigationBarItem(
                icon = {
                    Icon(
                        Icons.Default.Home,
                        contentDescription = "Home",
                        tint = secondaryColor
                    )
                },
                label = { Text("Home") },
                selected = currentRoute(navController) == "home",
                onClick = { navigateTo(navController, "home") },
                colors = NavigationBarItemColors(
                    selectedIconColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.secondary,
                    selectedIndicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = Color.DarkGray,
                    unselectedTextColor = MaterialTheme.colorScheme.secondary,
                    disabledIconColor = Color.DarkGray,
                    disabledTextColor = MaterialTheme.colorScheme.secondary
                )
            )
            NavigationBarItem(
                icon = {
                    Icon(
                        Icons.AutoMirrored.Filled.List,
                        contentDescription = "List",
                        tint = secondaryColor
                    )
                },
                label = { Text("List") },
                selected = currentRoute(navController) == "list",
                onClick = { navigateTo(navController, "list")
                },
                colors = NavigationBarItemColors(
                    selectedIconColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.secondary,
                    selectedIndicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = Color.DarkGray,
                    unselectedTextColor = MaterialTheme.colorScheme.secondary,
                    disabledIconColor = Color.DarkGray,
                    disabledTextColor = MaterialTheme.colorScheme.secondary
                )
            )
            NavigationBarItem(
                icon = {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = secondaryColor
                    )
                },
                label = { Text("Profile") },
                selected = currentRoute(navController) == "profile",
                onClick = { navigateTo(navController, "profile") },
                colors = NavigationBarItemColors(
                    selectedIconColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.secondary,
                    selectedIndicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = Color.DarkGray,
                    unselectedTextColor = MaterialTheme.colorScheme.secondary,
                    disabledIconColor = Color.DarkGray,
                    disabledTextColor = MaterialTheme.colorScheme.secondary
                )
            )
        }
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