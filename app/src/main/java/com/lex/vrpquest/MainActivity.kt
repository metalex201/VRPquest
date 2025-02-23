package com.lex.vrpquest


import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
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
import androidx.compose.material3.Button
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.lex.vrpquest.Managers.DonateGame
import com.lex.vrpquest.Managers.DonateQueue
import com.lex.vrpquest.Managers.DownloadService
import com.lex.vrpquest.Managers.Game
import com.lex.vrpquest.Managers.QueueGame
import com.lex.vrpquest.Managers.SortGameList
import com.lex.vrpquest.Managers.StartDonation
import com.lex.vrpquest.Managers.Startinstall
import com.lex.vrpquest.Managers.getDonateGames
import com.lex.vrpquest.Managers.isServiceRunning
import com.lex.vrpquest.Managers.startservice
import com.lex.vrpquest.Pages.DonatePage
import com.lex.vrpquest.Pages.LoadPage
import com.lex.vrpquest.Pages.MainPage
import com.lex.vrpquest.Pages.PermissionPage
import com.lex.vrpquest.Pages.QueuePage
import com.lex.vrpquest.Pages.SettingsPage
import com.lex.vrpquest.Pages.gameInfoPage
import com.lex.vrpquest.Utils.CircleButton
import com.lex.vrpquest.Utils.FullText
import com.lex.vrpquest.Utils.REQUEST_PERMISSION_RESULT_LISTENER
import com.lex.vrpquest.Utils.SearchBar
import com.lex.vrpquest.Utils.SettingGetSting
import com.lex.vrpquest.Utils.SettingGetStringSet
import com.lex.vrpquest.Utils.TextBar
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku
import java.io.File

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

var CustomColorScheme =
    darkColorScheme(
        //background = Color(0, 0, 0, 100),
        background = Color(27, 43, 59, 255), //Color(27, 43, 59, 255),
        surface = Color(50,63,76,255),
        error = Color(187,0,19,255),
        onSurface = Color(240,244,245,255),
        onSurfaceVariant = Color(54,63,78,255),
        tertiary = Color(3,100,237,255)
    )


var DarkTheme =
    darkColorScheme(
        background = Color(27, 43, 59, 255),
        surface = Color(50,63,76,255),
        onSurface = Color(240,244,245,255),
        tertiary = Color(3,100,237,255),
        error = Color(187,0,19,255)
    )

var LightTheme =
    darkColorScheme(
        background = Color(191, 197, 209, 255),
        surface = Color(182, 189, 199,255),
        onSurface = Color(9,20,24,255),
        tertiary = Color(0,102,231,255),
        error = Color(187,0,19,255)
    )

var TransparentTheme =
    darkColorScheme(
        background = Color(20, 20, 20, 30),
        surface = Color(50,63,76,255),
        onSurface = Color(240,244,245,255),
        tertiary = Color(3,100,237,255),
        error = Color(187,0,19,255)
    )


class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //KEEP DEVICE AWAKE
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            //THEME

            var themeid = SettingGetSting(applicationContext, "theme") ?: "dark"

            when(themeid) {
                "dark" -> { CustomColorScheme = DarkTheme}
                "light" -> { CustomColorScheme = LightTheme}
                "transparent" -> { CustomColorScheme = TransparentTheme}
            }

            //SettingStoreStringSet(applicationContext, "DonateBlacklist", setOf("te", "tes", "test"))
            val test = SettingGetStringSet(applicationContext, "DonateBlacklist")
            println(test)

            var Page by remember { mutableStateOf(intent.getIntExtra("page", 2)) }

            var GameInfo by remember { mutableStateOf<Game?>(null) }

            var Gamelist:MutableList<Game> = remember { mutableStateListOf() }
            var Queuelist:MutableList<QueueGame> = remember { mutableStateListOf() }
            var DonateQueueList:MutableList<DonateQueue> = remember { mutableStateListOf() }


            if (Page != 2) { //do not reinstall metadata just parse it when the settings page is loaded not main
                Gamelist = SortGameList(applicationContext, {})
            }

            var IsQueuelistEmpty by remember { mutableStateOf(false) }
            var IsShizukuConnected by remember { mutableStateOf(Shizuku.pingBinder() && com.lex.vrpquest.Utils.checkPermission(0)) }    // either shizuku doesnt exist or the app doesnt have permission

            LaunchedEffect(!IsShizukuConnected) {
                while(!IsShizukuConnected) {
                    delay(100)
                    IsShizukuConnected = Shizuku.pingBinder() && (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED)
                }
            }

            var searchText by remember { mutableStateOf("") }
            var sortType by remember { mutableStateOf(0) }
            var sortReversed by remember { mutableStateOf(false) }

            MaterialTheme(colorScheme = CustomColorScheme) {
                Surface(modifier = Modifier
                    .fillMaxSize()
                    .scale(1F), color = CustomColorScheme.background) {
                    LaunchedEffect(Queuelist.isEmpty()) {
                        IsQueuelistEmpty = Queuelist.isNotEmpty()
                        DonateQueueList.isNotEmpty()
                    }

                    if(Page == 0 || Page == 1 || Page == 3 ) {
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

                                        when(Page) {
                                            0 -> {
                                                SearchBar(Modifier.weight(0.1f), searchText, {searchText = it}, "Search here", {sortType = it},  {sortReversed = it})
                                            }
                                            1 -> {
                                                TextBar(Modifier.weight(0.1f), "Queue List")
                                            }
                                            3 -> {
                                                TextBar(Modifier.weight(0.1f), "Settings")
                                            }
                                        }

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
                                    .padding(10.dp, 0.dp)
                                ) {


                                    Box(modifier = if(Page == 0) Modifier.fillMaxSize()  else Modifier
                                        .fillMaxWidth()
                                        .alpha(0F)
                                    ) {
                                        if(Gamelist.isEmpty()) {
                                            FullText("Game List is currently empty, gamedata is either deleted or there was an issue connecting to the VRP server")
                                        } else {
                                            MainPage(Gamelist, searchText, {GameInfo = it; Page = 6}, sortType, sortReversed)
                                        }  //MAINPAGE ALWAYS COMPOSED
                                    }


                                    if (Page != 0) { // OVERLAY OVER MAINPAGE
                                        Box(modifier = Modifier
                                            .fillMaxSize()
                                            .pointerInput(Unit) {}) { // AVOID PASSING CLICKS THROUGH TO MAINPAGE
                                            when(Page) {
                                                1 -> {
                                                    if(Queuelist.isEmpty() && DonateQueueList.isEmpty()) {
                                                        FullText("Queue List is currently empty, you can start an install through tapping on an app and tapping on install")
                                                    } else { QueuePage(Queuelist, DonateQueueList)}
                                                }

                                                3 -> {SettingsPage({Page = 0; Page = 3})}
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    var donatelist= remember {mutableListOf<DonateGame>()}

                    if (Page == 2)
                        LoadPage({ getDonateGames(applicationContext, Gamelist).forEach() { donatelist.add(it) }
                                if (!(donatelist.size == 0)) { Page = 4 } else { Page = 0 } }, //avoid loading donatelist if its empty
                            { Gamelist = it.toMutableList()}) // FOR LOADING META.7z

                    if (Page == 4) {
                        DonatePage({Page = 0 },
                                {
                                    it.forEach() {
                                    DonateQueueList.add(DonateQueue(packageName = it.packageName, IsActive = mutableStateOf(false)))
                                }
                                StartDonation(applicationContext, DonateQueueList)
                            }, donatelist)
                    }

                    //downloadViewModel = ViewModelProvider(this).get(DownloadViewModel::class.java)
                    val liveData = DownloadService._queueList
                    // Observe the queue list
                    liveData.observe(this, Observer { queue ->
                        Queuelist.clear()
                        if(queue.isNotEmpty()) {
                            Queuelist.addAll(queue)
                        }
                    })

                    if (Page == 6) {
                        gameInfoPage(GameInfo, { game ->
                            var IsInQueue = false
                            for(i in Queuelist) {
                                if (i.game.PackageName == game.PackageName) {
                                     IsInQueue = true
                                }
                            }
                            if(!IsInQueue) {
                                val templist = liveData.value ?: mutableListOf()
                                templist.add(QueueGame(game))
                                liveData.postValue(templist)
                                startservice(applicationContext)
                            }
                            GameInfo = null
                            Page = 0
                            }, {Page = 0}
                        )
                    }
                    PermissionPage(Page, {Page = it})
                }
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        Shizuku.removeRequestPermissionResultListener(REQUEST_PERMISSION_RESULT_LISTENER)
    }
}

class ApkActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

        }
    }
}