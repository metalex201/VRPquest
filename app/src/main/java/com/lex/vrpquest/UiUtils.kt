package com.lex.vrpquest

import android.content.ContentValues.TAG
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.io.File
import java.util.Locale

@Composable
fun TextFieldElement(modifier: Modifier, value: String, onValueChange: (String) -> Unit, placeholder: String) {
    Box(modifier = Modifier.fillMaxWidth()) {
        TextField(
            modifier = modifier,
            value = value,
            onValueChange = onValueChange,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                cursorColor = CustomColorScheme.onSurface),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
            textStyle = TextStyle(textAlign = TextAlign.Start,
                textDecoration = TextDecoration.Underline),
            placeholder = {
                Text(text = placeholder,
                    modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .align(Alignment.CenterStart), color = CustomColorScheme.onSurface)
            }
        )
    }
}
@Composable
fun SearchBar(modifier: Modifier, value: String, onValueChange: (String) -> Unit, placeholder: String) {
    Box(modifier = modifier
        .fillMaxHeight()
        .fillMaxWidth()
        .clip(CircleShape)
        .background(CustomColorScheme.surface)) {
        TextFieldElement(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center)
                .clip(CircleShape),
            value = value,
            onValueChange = onValueChange,
            placeholder)

    }
}
@Composable
fun CircleButton(
    modifier:Modifier = Modifier,
    color: Color = CustomColorScheme.surface,
    Icon: ImageVector = Icons.Default.Edit,
    onClick: () -> Unit,
    IsDoted: Boolean = false
) {
    Box(modifier = modifier
        .fillMaxHeight()
        .aspectRatio(1f)
    ) {
        Box(modifier = Modifier
            .fillMaxSize()
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(color = color)
            .clickable { onClick() },
        ) { Icon(
            modifier = Modifier.align(Alignment.Center),
            imageVector = Icon,
            contentDescription = "")
        }
        if (IsDoted) {
            Box(modifier = Modifier
                .align(Alignment.TopEnd)
                .size(20.dp)
                .clip(CircleShape)
                .background(CustomColorScheme.tertiary)) {
            }
        }
    }
}

@Composable
fun AppGridList(Gamelist: MutableList<Game>, searchText: String, onClick: (Game) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 250.dp)
    ) {
        Log.i(TAG, Gamelist.count().toString())
        items(Gamelist.filter { it.GameName.contains(searchText, ignoreCase = true) }) { game ->
            Box(modifier = Modifier
                .width(280.dp)
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
fun DownloadingList(Queuelist: MutableList<QeueGame>) {
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
                            0 -> { Text(text = "Downloading.")
                            }
                            1 -> { Text(text = "Extracting..")
                            }
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
fun GetGameBitmap(thumbnail: String): ImageBitmap {
    if (File(thumbnail).exists()) {
        return BitmapFactory.decodeFile(thumbnail).asImageBitmap()
    } else {
        val EmptyThumb: Bitmap = Bitmap.createBitmap(374, 214, Bitmap.Config.ARGB_8888)
        EmptyThumb.eraseColor(CustomColorScheme.background.toArgb())
        return EmptyThumb.asImageBitmap()
    }
}

fun RoundByteValue(bytes:Int) : String {
    if (bytes < 1000) { return "$bytes MB"
    } else {
        return (bytes / 1000).toString() + " GB"
    }
}