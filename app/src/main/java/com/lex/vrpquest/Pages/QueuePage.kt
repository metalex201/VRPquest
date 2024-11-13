package com.lex.vrpquest.Pages

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.lex.vrpquest.Utils.CircleButton
import com.lex.vrpquest.CustomColorScheme
import com.lex.vrpquest.Managers.DonateQueue
import com.lex.vrpquest.Managers.RemoveDonatedGame
import com.lex.vrpquest.Utils.GetGameBitmap
import com.lex.vrpquest.Managers.QueueGame
import com.lex.vrpquest.Managers.RemoveQueueGame
import com.lex.vrpquest.Utils.GroupDropDown
import com.lex.vrpquest.Utils.RoundDivider
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun QueuePage(Queuelist: MutableList<QueueGame>, Donatelist: MutableList<DonateQueue>) {
    val context = LocalContext.current

    var isGamelistDD by remember { mutableStateOf(true) }
    var isDonatelistDD by remember { mutableStateOf(true) }
    var isHalffinishDD by remember { mutableStateOf(true) }

    val scrollState = rememberLazyListState()
    var scrollfloat by remember { mutableStateOf(((scrollState.firstVisibleItemIndex / 5 ) * 226) + scrollState.firstVisibleItemScrollOffset) }
    LaunchedEffect(scrollState.firstVisibleItemIndex, scrollState.firstVisibleItemScrollOffset) {
        scrollfloat = ((scrollState.firstVisibleItemIndex / 1 ) * 226) + scrollState.firstVisibleItemScrollOffset
        if (scrollfloat > 100) {scrollfloat = 100}
    }

    LazyColumn(
        state = scrollState,
        modifier = Modifier
            .graphicsLayer { alpha = 0.99f }
            .drawWithContent {
                val colors = listOf(
                    Color.Transparent,
                    Color.Black
                )
                drawContent()
                drawRect(
                    brush = Brush.verticalGradient(
                        colors, startY = 0f,
                        endY = scrollfloat.toFloat()
                    ),
                    blendMode = BlendMode.DstIn
                )
            },
    ) {
        if(!Donatelist.isEmpty()) {
            item{
                GroupDropDown(isDonatelistDD, {isDonatelistDD = it}, "Donations")
            }
        }
        if(isDonatelistDD) {
            itemsIndexed(Donatelist) {index, it ->
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .height(100.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(CustomColorScheme.surface))
                {
                    val appinfo = context.packageManager.getApplicationInfo(it.packageName.value,0)
                    val appname = appinfo.loadLabel(context.getPackageManager()).toString()
                    var bitmap: Bitmap

                    val icon: Drawable = appinfo.loadIcon(context.getPackageManager())
                    if (icon.intrinsicWidth == 0 || icon.intrinsicHeight == 0) {
                        bitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)
                    } else {
                        bitmap = icon.toBitmap() // Convert Drawable to Bitmap
                        if (icon.intrinsicWidth != 64) {
                            bitmap = Bitmap.createScaledBitmap(bitmap, 81, 81, false)
                        }
                    }

                    val imageBitmap = remember { bitmap.asImageBitmap() } // Remember the Bitmap


                    Row(Modifier.padding(15.dp)) {
                        Image(
                            bitmap = imageBitmap,
                            contentDescription = "Package Icon",
                            modifier = Modifier
                                .fillMaxHeight()
                                .clip(CircleShape)
                        )

                        Spacer(modifier = Modifier.size(20.dp))
                        Column(
                            Modifier
                                .weight(0.1f)
                                .padding(10.dp)) {
                           if(it.IsActive.value) {
                               Box(modifier = Modifier
                                   .fillMaxSize()
                                   .weight(0.1f)) {
                                   when(it.state.value) {
                                       0 -> { Text(text = "Zipping: $appname") }
                                       1 -> { Text(text = "Uploading: $appname") }
                                   }
                               }

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
                            else {
                               Box(modifier = Modifier
                                   .fillMaxSize()
                                   .weight(0.1f)) {
                                   Text(modifier = Modifier.align(Alignment.CenterStart),
                                       text = appname)
                               }
                            }
                        }
                        Spacer(modifier = Modifier.size(20.dp))
                        CircleButton(Icon = Icons.Default.Close, onClick = {
                            RemoveDonatedGame(index, Donatelist)
                        }, color = CustomColorScheme.error)

                    }
                }
            }
        }
        if(!Queuelist.isEmpty()) {
            item{
                GroupDropDown(isGamelistDD, {isGamelistDD = it}, "Game Installs")
            }
        }
        if(isGamelistDD) {
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
                        RoundDivider()
                        Spacer(modifier = Modifier.size(10.dp))
                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .padding(50.dp, 0.dp)) {
                            when(it.state.value) {
                                0 -> { Text(text = "Downloading.") }
                                1 -> { Text(text = "Extracting..") }
                                2 -> { Text(text = "Moving obb.") }
                                3 -> { Text(text = "Downloading obb...") }
                                4 -> { Text(text = "Downloading apk") }
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
}




