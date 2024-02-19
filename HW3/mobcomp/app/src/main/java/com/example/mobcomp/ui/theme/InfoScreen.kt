package com.example.mobcomp.ui.theme

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.mobcomp.AppDatabase
import com.example.mobcomp.User
import com.example.mobcomp.UserDao
import java.io.File
import java.io.FileOutputStream

@Composable
fun InfoScreen(onNavigateToHome: () -> Unit, database: AppDatabase, applicationContext: Context) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "info screen", fontSize = 30.sp)
        val userDao = database.userDao()
        GetContentExample(Modifier, applicationContext)
        SimpleOutlinedTextFieldSample(userDao)
        Button(onClick = { onNavigateToHome() }) {
            Text(text = "Back to Home screen")
        }
    }
}
@Composable
fun GetContentExample(Modifier: Modifier.Companion, applicationContext: Context) {
    val resolver = applicationContext.contentResolver
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
    }
    Column {
        Button(onClick = { launcher.launch("image/*") }) {
            Text(text = "Load Image")
        }
        Image(
            painter = rememberAsyncImagePainter(imageUri),
            contentDescription = null,
            modifier = Modifier
                    .size(100.dp)
                .clip(RectangleShape)
                .clickable(onClick = {})
        )
    }
    //save image
    imageUri?.let { saveImageToStorage(applicationContext, it) }

}

fun saveImageToStorage(context: Context, imageUri: Uri) {
    val inputStream = context.contentResolver.openInputStream(imageUri)
    val outputStream = FileOutputStream(File(context.filesDir, "image.jpg"))
    inputStream?.use { input ->
        outputStream.use { output ->
            input.copyTo(output)
        }
    }

}

@Composable
fun SimpleOutlinedTextFieldSample(userDao: UserDao) {
    val userText = getUserName(userDao)
    var text by remember { mutableStateOf(userText) }

    OutlinedTextField(
        value = text,
        onValueChange = { text = it }
        //label = { Text("Label")
        //}
    )
    saveOutput(userDao, text)
}

fun saveOutput(userDao: UserDao, text: String) {
    userDao.deleteId(0)
    userDao.insertAll(User(0, text))
}