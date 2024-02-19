package com.example.mobcomp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MobcompTheme {
                NavigationAppHost(applicationContext)

            }
        }
    }
}

@Composable
fun NavigationAppHost(applicationContext: Context) {
    val db = Room.databaseBuilder(
        applicationContext,
        AppDatabase::class.java, "database-name1"
    ).allowMainThreadQueries().build()
    val userDao = db.userDao()
    //userDao.insertAll(User(0, "lex"))
    //val resolver = applicationContext.contentResolver
    //val userDao = db.userDao()
    //userDao.insertAll(User(2,"hello", "world"))
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
