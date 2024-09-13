package com.lex.vrpquest

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun MainPage(Gamelist: MutableList<Game>, IsQueueEmpty: Boolean, onClick: (Game) -> Unit, Page: (Int) -> Unit ) {
    var searchText by remember { mutableStateOf("") }
    Column() {
        Box(modifier = Modifier
            .height(80.dp)
            .padding(10.dp)
            .fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxSize()) {
                CircleButton(Icon = Icons.Default.Refresh,
                    onClick = {Page(1)}, IsDoted = IsQueueEmpty)
                Spacer(modifier = Modifier.width(10.dp))
                SearchBar(Modifier.weight(0.1f), searchText, {searchText = it}, "Search here")
                Spacer(modifier = Modifier.width(10.dp))
                CircleButton(Icon = Icons.Default.Settings, onClick = {Page(3)})
            }
        }
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(10.dp, 0.dp)) {
            AppGridList(Gamelist, searchText, {onClick(it)})
        }
    }
}

@Composable
fun QueuePage(Queuelist: MutableList<QeueGame>,  Page:(Int) -> Unit) {
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
                        Icon = Icons.Default.Close,
                        onClick = {Page(0)})
                    Spacer(modifier = Modifier.width(10.dp))
                    SearchBar(Modifier.weight(0.1f), "", {}, "Queue List")
                    Spacer(modifier = Modifier.width(10.dp))
                    CircleButton(Icon = Icons.Default.Settings, onClick = {})
                }
            }
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(10.dp, 0.dp)) {
                DownloadingList(Queuelist)
            }
        }
    }
}

@Composable
fun SettingsPage(Page: (Int) -> Unit, IsQueueEmpty: Boolean) {

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
                    CircleButton(Icon = Icons.Default.Refresh,
                        onClick = {Page(1)}, IsDoted = IsQueueEmpty)
                    Spacer(modifier = Modifier.width(10.dp))
                    SearchBar(Modifier.weight(0.1f), "", {}, "Settings")
                    Spacer(modifier = Modifier.width(10.dp))
                    CircleButton(Icon = Icons.Default.Close, onClick = {Page(0)})
                }
            }
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(10.dp, 0.dp)) {
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
        }
    }
}

@Composable
fun gameInfoPage(game:Game, OnInstall: (Game) -> Unit, OnClose: () -> Unit) {
    val freespace = getFreeStorageSpace()

    Box(modifier = Modifier
        .fillMaxSize()
        .background(color = CustomColorScheme.background)
        .pointerInput(Unit) {}) {
        Box(modifier = Modifier
            .padding(10.dp)
            .clip(RoundedCornerShape(50.dp))
            .width(320.dp)
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

