package com.lex.vrpquest


import android.content.ContentValues.TAG
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.BoringLayout
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import rikka.shizuku.Shizuku
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import java.io.File
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState

import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.properties.Delegates

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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Shizuku.addRequestPermissionResultListener(REQUEST_PERMISSION_RESULT_LISTENER);
        //showNotification(applicationContext, "TEST NOTIF")
        //checkPermission(0)
        //enableEdgeToEdge()

        setContent {
            //OBB TEST

            var Page by remember { mutableStateOf(2) } //2
            var GameInfo by remember { mutableStateOf<Game?>(null) }

            var Gamelist:MutableList<Game> = remember { mutableStateListOf() }
            var Queuelist:MutableList<QueueGame> = remember { mutableStateListOf() }

            var IsQueuelistEmpty by remember { mutableStateOf(false) }
            var IsShizukuConnected by remember { mutableStateOf(Shizuku.pingBinder() && checkPermission(0)) }    // either shizuku doesnt exist or the app doesnt have permission

            var searchText by remember { mutableStateOf("") }

            MaterialTheme(colorScheme = CustomColorScheme) {
                Surface(modifier = Modifier.fillMaxSize(), color = CustomColorScheme.background) {
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

                    if (Page == 2) LoadPage({Page = 0}, {Gamelist = it.toMutableList()})


                    gameInfoPage(GameInfo, { game ->
                         if(!Queuelist.any { it.game == game }) {
                             Queuelist.add(QueueGame(game))
                             Startinstall(applicationContext, Queuelist)
                         }
                         GameInfo = null
                         }, {GameInfo = null})

                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Shizuku.removeRequestPermissionResultListener(REQUEST_PERMISSION_RESULT_LISTENER)
    }
}

