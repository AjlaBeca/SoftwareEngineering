package com.example.cookbook.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.Log
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.android.identity.util.UUID
import com.example.cookbook.data.viewmodels.UserViewModel
import com.example.cookbook.R
import com.example.cookbook.ui.theme.Orange
import com.example.cookbook.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavHostController,
    userViewModel: UserViewModel,
    userId: Long
) {
    val currentUser by userViewModel.currentUser.observeAsState()
    val context = LocalContext.current

    var errorMessage by remember { mutableStateOf("") }
    var username by remember { mutableStateOf(currentUser?.username ?: "") }
    var newPassword by remember { mutableStateOf("") }
    var oldPassword by remember { mutableStateOf("") }
    var imageUri by remember {
        mutableStateOf(
            loadImageUriFromPreferences(
                context,
                currentUser?.userId ?: 0
            )
        )
    }
    var changePassword by remember { mutableStateOf(false) }
    var changeUsername by remember { mutableStateOf(false) }

    val openGallery =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { selectedImageUri ->
            selectedImageUri?.let { uri ->
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                viewModelScope.launch {
                    // Upload image to Firebase Storage
                    val storageRef = FirebaseStorage.getInstance().reference
                        .child("profile_images/${auth.currentUser?.uid ?: UUID.randomUUID()}")

                    try {
                        storageRef.putFile(uri).await()
                        val downloadUrl = storageRef.downloadUrl.await().toString()

                        // Update imageUri with Firebase Storage URL
                        imageUri = Uri.parse(downloadUrl)
                    } catch (e: Exception) {
                        Log.e("EditProfile", "Error uploading image: ${e.message}")
                    }
                }
            }
        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(30.dp)
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary)
                    .clickable {
                        openGallery.launch("image/*")
                    }
            ) {
                val painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .data(data = imageUri ?: R.drawable.profile)
                        .apply(block = fun ImageRequest.Builder.() {
                            placeholder(R.drawable.profile)
                        }).build()
                )

                Image(
                    painter = painter,
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )

                IconButton(
                    onClick = {
                        openGallery.launch("image/*")
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }


            Text(
                text = "Change Username >",
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        changeUsername = !changeUsername
                    }
                    .padding(vertical = 8.dp)
                    .padding(bottom = if (changeUsername) 16.dp else 0.dp)
            )

            if (changeUsername) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("New Username") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        cursorColor = MaterialTheme.colorScheme.secondary,
                        focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.tertiary,
                        focusedLabelColor = MaterialTheme.colorScheme.tertiary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.tertiary
                    ),
                )
            }


            Text(
                text = "Change Password >",
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        changePassword = !changePassword
                    }
                    .padding(vertical = 8.dp)
                    .padding(bottom = if (changePassword) 16.dp else 0.dp)
            )

            if (changePassword) {
                OutlinedTextField(
                    value = oldPassword,
                    onValueChange = { oldPassword = it },
                    label = { Text("Old Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        cursorColor = MaterialTheme.colorScheme.secondary,
                        focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.tertiary,
                        focusedLabelColor = MaterialTheme.colorScheme.tertiary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.tertiary
                    ),
                )
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        cursorColor = MaterialTheme.colorScheme.secondary,
                        focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.tertiary,
                        focusedLabelColor = MaterialTheme.colorScheme.tertiary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.tertiary
                    ),
                )
            }

            Button(
                onClick = {
                    if (!changePassword || (currentUser?.password == oldPassword && isPasswordValid(newPassword))) {
                        val updatedUser = currentUser!!.copy(
                            username = username,
                            image = imageUri.toString(),
                            password = if (changePassword) newPassword else currentUser!!.password
                        )
                        userViewModel.updateUserProfile(updatedUser)
                        navController.popBackStack()
                        errorMessage = ""
                    } else if (changePassword && !isPasswordValid(newPassword)) {
                        errorMessage = "Password must be at least 6 characters long, contain at least one number and one uppercase letter"
                    } else {
                        errorMessage = "Incorrect old password"
                    }
                    userViewModel.refreshCurrentUser()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .height(48.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = White
                ),
            ) {
                Text(
                    "Save Changes",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                )
            }

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

private fun loadImageUriFromPreferences(context: Context, userId: Long): Uri? {
    val sharedPref = context.getSharedPreferences("image_pref_$userId", Context.MODE_PRIVATE)
    val imageUriString = sharedPref.getString("image_uri", null)
    return imageUriString?.let { Uri.parse(it) }
}

private fun saveImageUriToPreferences(context: Context, userId: Long, uri: Uri) {
    val sharedPref = context.getSharedPreferences("image_pref_$userId", Context.MODE_PRIVATE)
    with(sharedPref.edit()) {
        putString("image_uri", uri.toString())
        apply()
    }
}

private fun isPasswordValid(password: String): Boolean {
    val passwordRegex = "^(?=.*[0-9])(?=.*[A-Z]).{6,}$".toRegex()
    return passwordRegex.matches(password)
}
