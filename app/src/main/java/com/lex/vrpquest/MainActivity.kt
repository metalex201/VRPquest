package com.lex.vrpquest


import android.content.Context
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.Global
import android.util.DisplayMetrics
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.datastore.core.DataStore
import rikka.shizuku.Shizuku
import java.io.File
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.lex.vrpquest.ui.theme.testfpt
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

var CustomColorScheme =
    darkColorScheme(
        background = Color(27, 43, 59, 255), //Color(27, 43, 59, 255),
        surface = Color(50,63,76,255),
        error = Color(187,0,19,255),
        onSurface = Color(240,244,245,255),
        onSurfaceVariant = Color(54,63,78,255),
        tertiary = Color(3,100,237,255)
    )

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        //Shizuku.addRequestPermissionResultListener(REQUEST_PERMISSION_RESULT_LISTENER);
        //showNotification(applicationContext, "TEST NOTIF")
        //checkPermission(0)
        //enableEdgeToEdge()


        //settingsTest
        //SettingStoreSting(applicationContext, "test", "TESTTESTTEST")
        //println("SETTINGS STORE GET RESULT : " + SettingGetSting(applicationContext, "test"))
        //SettingStoreBoolean(applicationContext, "test", true)
        //println("SETTINGS STORE GET RESULT : " + SettingGetSting(applicationContext, "test"))
        GlobalScope.launch { println("TESTFTP RESULT:" + testfpt("MTWMTGK80T72", "MKUSNAM63C96", "ganymede.whatbox.ca:11385")) }
        setContent {
            //OBB TEST
            //println("OBBTEST")
            //val res = File("/storage/emulated/0/Android/obb/TESTTT").mkdirs()
            //println("OBBTEST: $res")
            //println(ShizAdbCommand("echo testt"))



            var Page by remember { mutableStateOf(intent.getIntExtra("page", 2)) }

            var GameInfo by remember { mutableStateOf<Game?>(null) }

            var Gamelist:MutableList<Game> = remember { mutableStateListOf() }
            var Queuelist:MutableList<QueueGame> = remember { mutableStateListOf() }

            if (Page != 2) { //do not reinstall metadata just parse it when the settings page is loaded not main
                Gamelist = SortGameList(applicationContext, {})
            }

            var IsQueuelistEmpty by remember { mutableStateOf(false) }
            var IsShizukuConnected by remember { mutableStateOf(Shizuku.pingBinder() && checkPermission(0)) }    // either shizuku doesnt exist or the app doesnt have permission

            var searchText by remember { mutableStateOf("") }

            MaterialTheme(colorScheme = CustomColorScheme) {
                Surface(modifier = Modifier
                    .fillMaxSize()
                    .scale(1F), color = CustomColorScheme.background) {
                    LaunchedEffect(Queuelist.isEmpty()) {
                        IsQueuelistEmpty = Queuelist.isNotEmpty()
                    }
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .background(color = CustomColorScheme.background)
                        .pointerInput(Unit) {}) {
                        Column() {
                            Box(modifier = Modifier
                                .height(80.dp)
                                .padding(10.dp)
                                .fillMaxWidth()) {
                                Row(modifier = Modifier.fillMaxSize()) {
                                    CircleButton(
                                        Icon = if (Page == 1) Icons.Default.Close else Icons.Default.Refresh,
                                        onClick = {if (Page == 1) Page = 0 else Page = 1},
                                        IsDoted = IsQueuelistEmpty
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))

                                    //SearchBar(Modifier.weight(0.1f), "", {}, "Queue List")
                                    SearchBar(Modifier.weight(0.1f), searchText, {searchText = it}, "Search here")
                                    Spacer(modifier = Modifier.width(10.dp))
                                    CircleButton(Icon = if (Page == 3) Icons.Default.Close else Icons.Default.Settings,
                                        onClick = {if (Page == 3) Page = 0 else Page = 3},
                                        IsDoted = !IsShizukuConnected,
                                        DotColor = CustomColorScheme.error
                                    )
                                }
                            }
                            Box(modifier = Modifier
                                .fillMaxSize()
                                .padding(10.dp, 0.dp)) {
                                MainPage(Gamelist, searchText, {GameInfo = it}) //MAINPAGE ALWAYS COMPOSED
                                if (Page != 0) { // OVERLAY OVER MAINPAGE
                                    Box(modifier = Modifier
                                        .fillMaxSize()
                                        .background(color = CustomColorScheme.background)
                                        .pointerInput(Unit) {}) { // AVOID PASSING CLICKS THROUGH TO MAINPAGE
                                        when(Page) {
                                            0 -> {} //AVOID RELOADING MAINPAGE TO SAVE PROCESSING
                                            1 -> {QueuePage(Queuelist)}
                                            2 -> {} //LOADPAGE IS OVERLAYED NOT NESTED
                                            3 -> {SettingsPage()}
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (Page == 2) LoadPage({Page = 0}, {Gamelist = it.toMutableList()}) // FOR LOADING META.7z

                    gameInfoPage(GameInfo, { game ->
                         if(!Queuelist.any { it.game == game }) {
                             Queuelist.add(QueueGame(game))
                             Startinstall(applicationContext, Queuelist)
                         }
                         GameInfo = null
                         }, {GameInfo = null})

                    PermissionPage()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Shizuku.removeRequestPermissionResultListener(REQUEST_PERMISSION_RESULT_LISTENER)
    }
}

