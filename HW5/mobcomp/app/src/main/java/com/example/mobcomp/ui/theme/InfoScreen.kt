package com.example.mobcomp.ui.theme

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.rememberAsyncImagePainter
import com.example.mobcomp.AppDatabase
import com.example.mobcomp.User
import com.example.mobcomp.UserDao
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@Composable
fun InfoScreen(
    onNavigateToHome: () -> Unit,
    database: AppDatabase,
    applicationContext: Context,
    onNavigateToMap: () -> Unit
) {
    var weather by remember { mutableStateOf<WeatherResponse?>(null) }
    var latitude by remember { mutableStateOf("65.01221") }
    var longitude by remember { mutableStateOf("25.46164") }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "info screen", fontSize = 30.sp)
        val userDao = database.userDao()
        //GetContentExample(Modifier, applicationContext)
        //SimpleOutlinedTextFieldSample(userDao)

        var videoUri by remember { mutableStateOf<Uri?>(null) }
        val pickVideoResultLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
            videoUri = uri
        }

        Button(onClick = { pickVideoResultLauncher.launch("video/*") }) {
            Text(text = "Pick Video")
        }

        videoUri?.let { uri ->
            AndroidView(factory = { context ->
                VideoView(context).apply {
                    setVideoURI(uri)
                    start()
                }
            }, modifier = Modifier.size(200.dp, 200.dp))
        }

        var isRecording by remember { mutableStateOf(false) }
        var isPlaying by remember { mutableStateOf(false) }
        Box(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Button(onClick = {
                isRecording = !isRecording
                if (isRecording) {
                    startRecording(applicationContext)
                } else {
                    stopRecording()
                }
            }) {
                Text(text = if (isRecording) "Stop Recording" else "Start Recording")
            }
        }

        Box(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Button(onClick = {
                isPlaying = !isPlaying
                if (isPlaying) {
                    startPlayback()
                } else {
                    stopPlayback()
                }
            }) {
                Text(text = if (isPlaying) "Stop Playback" else "Start Playback")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OutlinedTextField(
                value = latitude,
                onValueChange = { latitude = it },
                label = { Text("Latitude") },
                modifier = Modifier.width(120.dp)
            )

            OutlinedTextField(
                value = longitude,
                onValueChange = { longitude = it },
                label = { Text("Longitude") },
                modifier = Modifier.width(120.dp)
            )
        }

        Button(onClick = {
            if (latitude.isNotEmpty() && longitude.isNotEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    val response = RequestModule.weatherService.getWeather(latitude.toDouble(), longitude.toDouble(), "temperature_2m")
                    withContext(Dispatchers.Main) {
                        weather = response
                    }
                }
            }
        }) {
            Text(text = "Get Weather")
        }
        weather?.let { weather ->
            Text(text = "Temperature: ${weather.current.temperature_2m} ${weather.current_units.temperature_2m} \n Latitude: ${weather.latitude} \n Longitude: ${weather.longitude}")
        }



        Button(onClick = { onNavigateToHome() }) {
            Text(text = "Back to Home screen")
        }
        Button(onClick = { onNavigateToMap() }) {
            Text(text = "Go to Map screen")
        }


        //val deviceSensors: List<Sensor> = sensorManager.getSensorList(Sensor.TYPE_ALL)
        //Text(text = deviceSensors.toString())
        //val sensor = lightSensor(sensorManager)
        //val mLight = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        //Text(text = mLight.toString())

    }
}

object RequestModule {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.open-meteo.com/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val weatherService: WeatherService = retrofit.create(WeatherService::class.java)
}
data class WeatherResponse(
    val latitude: Double,
    val longitude: Double,
    val generationtime_ms: Double,
    val utc_offset_seconds: Int,
    val timezone: String,
    val timezone_abbreviation: String,
    val elevation: Double,
    val current_units: CurrentUnits,
    val current: Current
)

data class CurrentUnits(
    val time: String,
    val interval: String,
    val temperature_2m: String
)

data class Current(
    val time: String,
    val interval: Int,
    val temperature_2m: Double
)
interface WeatherService {
    @GET("v1/forecast")
    suspend fun getWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String
    ): WeatherResponse
}



var mediaRecorder: MediaRecorder? = null
var mediaPlayer: MediaPlayer? = null
var output: String? = null

fun startRecording(ctx: Context) {
    output = ctx.getExternalFilesDir(null)?.absolutePath + "/recording.3gp"

    mediaRecorder = MediaRecorder().apply {
        setAudioSource(MediaRecorder.AudioSource.MIC)
        setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        setOutputFile(output)
    }

    mediaRecorder?.prepare()
    mediaRecorder?.start()  // Recording starts
}

fun stopRecording() {
    mediaRecorder?.apply {
        stop()
        release()
    }
    mediaRecorder = null
}

fun startPlayback() {
    mediaPlayer = MediaPlayer().apply {
        try {
            setDataSource(output)
            prepare()
            start()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}

fun stopPlayback() {
    mediaPlayer?.release()
    mediaPlayer = null
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
