package com.example.mobcomp.ui.theme

import SampleData
import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.mobcomp.AppDatabase
import com.example.mobcomp.R
import com.example.mobcomp.UserDao
import java.io.File

@Composable
fun HomeScreen(onNavigateToInfo: () -> Unit, database: AppDatabase, applicationContext: Context)  {
    Conversation( onNavigateToInfo = onNavigateToInfo, messages = SampleData.conversationSample, database = database, applicationContext = applicationContext)
}
data class Message(val author: String, val body: String)

@Composable
fun MessageCard(onNavigateToInfo: () -> Unit, msg: Message, database: AppDatabase, applicationContext: Context) {
    Row(modifier = Modifier.padding(all = 8.dp)) {

        loadImageFromStorage(onNavigateToInfo, context = applicationContext, fileName = "image.jpg")
        Spacer(modifier = Modifier.width(8.dp))

        // We keep track if the message is expanded or not in this
        // variable
        var isExpanded by remember { mutableStateOf(false) }
        // surfaceColor will be updated gradually from one color to the other
        val surfaceColor by animateColorAsState(
            if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        )
        val userDao = database.userDao()
        // We toggle the isExpanded variable when we click on this Column
        Column(modifier = Modifier.clickable { isExpanded = !isExpanded }) {
            Text(
                text = getUserName(userDao),
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.titleSmall
            )

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                shape = MaterialTheme.shapes.medium,
                shadowElevation = 1.dp,
                // surfaceColor color will be changing gradually from primary to surface
                color = surfaceColor,
                // animateContentSize will change the Surface size gradually
                modifier = Modifier
                    .animateContentSize()
                    .padding(1.dp)
            ) {
                Text(
                    text = msg.body,
                    modifier = Modifier.padding(all = 4.dp),
                    // If the message is expanded, we display all its content
                    // otherwise we only display the first line
                    maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}



@Composable
fun Conversation(onNavigateToInfo: () -> Unit, messages: List<Message>, database: AppDatabase, applicationContext: Context) {
    LazyColumn {
        items(messages) { message ->
            MessageCard(onNavigateToInfo, message, database, applicationContext)
        }
    }
}
@Composable
fun loadImageFromStorage(onNavigateToInfo: () -> kotlin.Unit, context: Context, fileName: String) {
    val file = File(context.filesDir, fileName)
    if (file.exists()) {
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(1.5.dp, MaterialTheme.colorScheme.secondary, CircleShape)
                .clickable( onClick = { onNavigateToInfo() })
        )
    } else {
        Image(
            painter = painterResource(R.drawable.androidtest),
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(1.5.dp, MaterialTheme.colorScheme.secondary, CircleShape)
                .clickable( onClick = { onNavigateToInfo() })
        )
    }
}

fun getUserName(userDao: UserDao): String {
    var name = ""
    if(userDao.getAll().size > 0) {
        name = userDao.getAll()[0].firstName.toString()
    } else {
        name = "Lex"
    }
    return name
}