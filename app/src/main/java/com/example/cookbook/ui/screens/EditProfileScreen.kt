package com.example.cookbook.ui.screens
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.Modifier.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.cookbook.data.viewmodels.UserViewModel
import com.example.cookbook.ui.theme.*
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.DropdownMenu
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import coil.compose.rememberImagePainter
import android.content.Intent


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
    var imageUri by remember { mutableStateOf(loadImageUriFromPreferences(context, currentUser!!.userId)) }
    var changePassword by remember { mutableStateOf(false) }
    var changeUsername by remember { mutableStateOf(false) }

    val openGallery =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { selectedImageUri ->
            selectedImageUri?.let { uri ->
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                imageUri = uri
                saveImageUriToPreferences(context, currentUser!!.userId, uri)
            }
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(Color.Gray)
                .clickable {
                    openGallery.launch("image/*")
                }
        ) {
            imageUri?.let {
                val painter = rememberImagePainter(data = it)

                Image(
                    painter = painter,
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            }

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
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        // Toggle for Change Username
        Text(
            text = "Change Username >",
            color = Color.White,
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
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Toggle for Change Password
        Text(
            text = "Change Password >",
            color = Color.White,
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
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("New Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Button(
            onClick = {
                if (!changePassword || currentUser?.password == oldPassword) {
                    val updatedUser = currentUser!!.copy(
                        username = username,
                        image = imageUri.toString(),
                        password = if (changePassword) newPassword else currentUser!!.password
                    )
                    userViewModel.updateUserProfile(updatedUser)
                    navController.popBackStack()
                    errorMessage = ""
                } else {
                    errorMessage = "Incorrect old password"
                }
                userViewModel.refreshCurrentUser()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .height(48.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Save Changes")
        }

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                modifier = Modifier.padding(top = 8.dp)
            )
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
