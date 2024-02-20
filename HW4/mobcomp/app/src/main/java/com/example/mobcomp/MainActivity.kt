package com.example.mobcomp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.mobcomp.ui.theme.HomeScreen
import com.example.mobcomp.ui.theme.InfoScreen
import com.example.mobcomp.ui.theme.MobcompTheme

class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var light: Sensor? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MobcompTheme {
                NavigationAppHost(applicationContext)

            }
        }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        createNotificationChannel()
        promptForNotificationPermission()

    }

    override fun onResume() {
        super.onResume()
        light?.also { light ->
            sensorManager.registerListener(this, light, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_LIGHT) {
            //promptForNotificationPermission()
            notifyUser("Light sensor value changed: ${event.values[0]}")
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Do something here if sensor accuracy changes.
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun notifyUser(message: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, "lightSensorChannel")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("Light Sensor Update")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        with(NotificationManagerCompat.from(this)) {
            //promptForNotificationPermission()
            notify(123, builder.build())
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Light Sensor Channel"
            val descriptionText = "Channel for light sensor notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("lightSensorChannel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun promptForNotificationPermission() {
        val areNotificationsEnabled = NotificationManagerCompat.from(this).areNotificationsEnabled()
        if (!areNotificationsEnabled) {
            AlertDialog.Builder(this)
                .setTitle("Notifications")
                .setMessage("Notifications are disabled. Do you want to enable them?")
                .setPositiveButton("Yes") { dialog, which ->
                    // Open app settings
                    val intent = Intent(ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                    }
                    startActivity(intent)
                }
                .setNegativeButton("No") { dialog, which ->
                    // User disagreed, do nothing
                }
                .show()
        }
    }
    private fun areNotificationsEnabled(): Boolean {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = notificationManager.getNotificationChannel("lightSensorChannel")
            channel?.importance != NotificationManager.IMPORTANCE_NONE
        } else {
            NotificationManagerCompat.from(this).areNotificationsEnabled()
        }
    }

}

@Composable
fun NavigationAppHost(applicationContext: Context) {
    val db = Room.databaseBuilder(
        applicationContext,
        AppDatabase::class.java, "database-name13"
    ).allowMainThreadQueries().build()
    val userDao = db.userDao()
    //userDao.insertAll(User(0, "lex"))
    //val resolver = applicationContext.contentResolver
    //val userDao = db.userDao()
    //userDao.insertAll(User(2,"hello", "world"))
    //test(applicationContext)

    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("info") {
            InfoScreen(onNavigateToHome = {
                navController.navigate("home") {
                    popUpTo("home") {
                        inclusive = true
                    }
                }
            }, database = db, applicationContext = applicationContext
            )
        }
        composable("home") {
            HomeScreen(onNavigateToInfo = {
                    navController.navigate("info")
                }, database = db, applicationContext = applicationContext
            )
        }
    }
}

@Entity
data class User(
    @PrimaryKey val uid: Int,
    @ColumnInfo(name = "first_name") val firstName: String?
    //@ColumnInfo(name = "last_name") val lastName: String?
)

@Dao
interface UserDao {
    @Query("SELECT * FROM user")
    fun getAll(): List<User>

    @Query("SELECT * FROM user WHERE uid IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<User>

    @Insert
    fun insertAll(vararg users: User)

    @Delete
    fun delete(user: User)

    @Query("DELETE FROM user WHERE uid = (:userId)")
    fun deleteId(userId: Int)

}

@Database(entities = [User::class, Comment::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): com.example.mobcomp.UserDao
    abstract fun commentDao(): CommentDao
}


@Dao
interface CommentDao {
    @Query("SELECT * FROM comment")
    fun getAll(): List<Comment>

    @Insert
    fun insertAll(vararg comment: Comment)

    @Delete
    fun delete(comment: Comment)
}
@Entity
data class Comment(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name= "comment") val comment: String?
)
