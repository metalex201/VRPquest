package com.lex.vrpquest

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat.startActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun MainPage(Gamelist: MutableList<Game>, searchText: String, onClick: (Game) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 200.dp)
    ) {
        Log.i(TAG, Gamelist.count().toString())
        items(Gamelist.filter { it.GameName.contains(searchText, ignoreCase = true) }) { game ->
            Box(modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(
                    CustomColorScheme.surface
                )
                .clickable { onClick(game) }) {
                Column(Modifier.padding(10.dp)) {
                    Image(modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(40.dp)),
                        bitmap = GetGameBitmap(game.Thumbnail),
                        contentDescription = "")
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(text = game.GameName,
                        modifier = Modifier.padding(20.dp, vertical = 0.dp),
                        maxLines = 1, overflow = TextOverflow.Ellipsis,)
                    Text(text = RoundByteValue(game.Size),
                        modifier = Modifier.padding(20.dp, vertical = 0.dp),
                        maxLines = 1, overflow = TextOverflow.Ellipsis,)
                }
            }
        }
    }
}

@Composable
fun QueuePage(Queuelist: MutableList<QueueGame>) {
    LazyColumn(
    ) {
        itemsIndexed(Queuelist) { index, it ->
            var IsDropDown by remember { mutableStateOf(false) }
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(CustomColorScheme.surface))
            {
                Box(modifier = Modifier
                    .height(150.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50.dp))) {
                    Row(Modifier.padding(10.dp)) {
                        Image(modifier = Modifier
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(40.dp))
                            .align(Alignment.CenterVertically),
                            bitmap = GetGameBitmap(it.game.Thumbnail),
                            contentDescription = "")
                        Spacer(modifier = Modifier.size(20.dp))
                        Column(
                            Modifier
                                .weight(0.1f)
                                .padding(20.dp)) {
                            Box(modifier = Modifier
                                .fillMaxSize()
                                .weight(0.1f)) {
                                Text(modifier = Modifier.align(Alignment.CenterStart),
                                    text = it.game.GameName)
                            }
                            if(it.IsActive.value) {
                                Box(modifier = Modifier
                                    .fillMaxSize()
                                    .weight(0.1f)) {
                                    LinearProgressIndicator(
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .fillMaxWidth(),
                                        color = CustomColorScheme.tertiary,
                                        trackColor = CustomColorScheme.onSurface,
                                        progress = { it.MainProgress.value })
                                }
                            }
                        }
                        if(it.IsActive.value) {
                            Spacer(modifier = Modifier.size(20.dp))
                            CircleButton(modifier = Modifier.padding(0.dp, 30.dp),
                                Icon = if (IsDropDown) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                onClick = { IsDropDown = !IsDropDown }, color = CustomColorScheme.tertiary)
                        }
                        CircleButton(modifier = Modifier.padding(30.dp), Icon = Icons.Default.Close, onClick = {
                            RemoveQueueGame(index, Queuelist)
                        }, color = CustomColorScheme.error)
                    }
                }
                if(IsDropDown) {
                    Spacer(modifier = Modifier.size(10.dp))
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .padding(50.dp, 0.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(color = CustomColorScheme.background))
                    Spacer(modifier = Modifier.size(10.dp))
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .padding(50.dp, 0.dp)) {
                        when(it.state.value) {
                            0 -> { Text(text = "Downloading.") }
                            1 -> { Text(text = "Extracting..") }
                            2 -> { Text(text = "Moving obb.") }
                        }
                    }

                    for(i in it.progressList.indices) {
                        Spacer(modifier = Modifier.size(20.dp))
                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .padding(50.dp, 0.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "7z." + String.format(Locale.getDefault(), "%03d", i + 1))
                            Spacer(modifier = Modifier.size(10.dp))
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth(),
                                color = CustomColorScheme.tertiary,
                                trackColor = CustomColorScheme.onSurface,
                                progress = { it.progressList[i] })
                        }
                    }
                    Spacer(modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun SettingsPage() {
    Column(modifier = Modifier
        .fillMaxWidth()) {
        Box(modifier = Modifier
            .padding(10.dp)
            .clip(RoundedCornerShape(50.dp))
            .fillMaxWidth()
            .background(color = CustomColorScheme.surface)
            ) {
            Text(modifier = Modifier.padding(25.dp),
                text = "Shizuku is not running")
        }
    }
}

@Composable
fun gameInfoPage(game:Game?, OnInstall: (Game) -> Unit, OnClose: () -> Unit) {
    if (game != null) {
        val freespace = getFreeStorageSpace()

        Box(modifier = Modifier
            .fillMaxSize()
            .background(color = CustomColorScheme.background)
            .pointerInput(Unit) {}) {
            Box(modifier = Modifier
                .padding(10.dp)
                .clip(RoundedCornerShape(50.dp))
                .width(280.dp)
                .align(Alignment.Center)
                .background(
                    CustomColorScheme.surface
                )) {
                Column(Modifier.padding(10.dp)) {
                    Image(modifier = Modifier
                        .clip(RoundedCornerShape(40.dp)),
                        bitmap = GetGameBitmap(game.Thumbnail),
                        contentDescription = "")

                    Spacer(modifier = Modifier.size(10.dp))
                    Text(text = "name: " + game.GameName)
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(text = "release Name: " + game.ReleaseName,)
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(text = "Version Code: " + game.VersionCode,)
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(text = "Last Updated: " + game.LastUpdated,)
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(text = "size: " + RoundByteValue(game.Size),)
                    Spacer(modifier = Modifier.size(10.dp))


                    println(freespace / 1000000)
                    println(game.Size * 2)
                    Row(Modifier.padding(10.dp, 0.dp)) {
                        if (game.Size * 2 < freespace / 1000000) {
                            Button(modifier = Modifier
                                .fillMaxWidth()
                                .weight(0.1F)
                                .height(50.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CustomColorScheme.tertiary,
                                    contentColor = CustomColorScheme.onSurface),
                                onClick = {OnInstall(game)}, ) { Text(text = "INSTALL") }
                        } else {
                            Button(modifier = Modifier
                                .fillMaxWidth()
                                .weight(0.1F)
                                .height(50.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CustomColorScheme.error,
                                    contentColor = CustomColorScheme.onSurface),
                                onClick = {}, ) { Text(text = "NO SPACE") }
                        }

                        Spacer(modifier = Modifier.size(10.dp))
                        Button(modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.1F)
                            .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CustomColorScheme.error,
                                contentColor = CustomColorScheme.onSurface),
                            onClick = {OnClose()}, ) { Text(text = "CLOSE") }

                    }
                    Spacer(modifier = Modifier.size(10.dp))
                }

            }
        }
    }
}

@Composable
fun LoadPage(Page: (Int) -> Unit, metadata: (ArrayList<Game>) -> Unit) {
    val context = LocalContext.current

    var progress by remember { mutableStateOf(0f) }
    var isRun by remember { mutableStateOf(false) }
    var state by remember { mutableStateOf(0) }
    var text by remember { mutableStateOf("") }

    if (!isRun) {
        MetadataInitialize(context, {state = it }, {progress = it}, {metadata(it)})
        isRun = true
    }

    when(state) {
        0 -> { text = "Downloading Metadata"}
        1 -> { text = "Extracting Metadata"}
        2 -> { text = "Parsing Metadata"}
        3 -> { Page(0)}
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(color = CustomColorScheme.background)
        .pointerInput(Unit) {}) {
        Box(modifier = Modifier
            .align(Alignment.Center)
            .width(300.dp)
            .clip(RoundedCornerShape(50.dp))
            .background(color = CustomColorScheme.surface)
        ) {
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(50.dp, 25.dp)) {
                Text(modifier = Modifier.align(Alignment.Start), text = text)
                Spacer(modifier = Modifier.size(25.dp))
                LinearProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .fillMaxWidth(),
                    color = CustomColorScheme.tertiary,
                    trackColor = CustomColorScheme.onSurface,
                    progress = { progress })
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun PermissionPage() {
    val context = LocalContext.current
    var InstallApkPerm by remember { mutableStateOf(context.packageManager.canRequestPackageInstalls()) }

    LaunchedEffect(true) {
        while(true) {
            delay(10)
            InstallApkPerm = context.packageManager.canRequestPackageInstalls()

            if (InstallApkPerm) {
                break
            }
        }
        cancel()
    }


    fun grant() {
        if (!InstallApkPerm) {
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                .setData(Uri.parse(String.format("package:%s", context.packageName)))
            context.startActivity(intent)
        }

        //startActivityForResult()
    }
    if (!(InstallApkPerm)) {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(color = CustomColorScheme.background)
            .pointerInput(Unit) {}) {
            Box(modifier = Modifier
                .padding(10.dp)
                .clip(RoundedCornerShape(50.dp))
                .width(280.dp)
                .align(Alignment.Center)
                .background(
                    CustomColorScheme.surface
                )) {
                Column(Modifier.padding(20.dp)) {
                    Text(text = "These permissions are required by this application:")
                    Spacer(modifier = Modifier.size(20.dp))
                        Text(modifier = Modifier.padding(20.dp,0.dp),
                            color = if (InstallApkPerm) CustomColorScheme.tertiary else CustomColorScheme.error,
                            text = "- INSTALL APK")

                    Spacer(modifier = Modifier.size(20.dp))
                    Row() {
                        Button(modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.1F)
                            .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CustomColorScheme.tertiary,
                                contentColor = CustomColorScheme.onSurface),
                            onClick = {grant()}, ) { Text(text = "GRANT") }
                        Spacer(modifier = Modifier.size(10.dp))
                        Button(modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.1F)
                            .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CustomColorScheme.error,
                                contentColor = CustomColorScheme.onSurface),
                            onClick = {System.out.close() }, ) { Text(text = "EXIT") }
                    }
                }
            }
        }
    }
}