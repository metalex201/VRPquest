package com.lex.vrpquest.Pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.LineHeightStyle
import com.lex.vrpquest.CustomColorScheme
import com.lex.vrpquest.Managers.FTPconnect
import com.lex.vrpquest.Managers.FTPfindApk
import com.lex.vrpquest.Managers.Game
import com.lex.vrpquest.Utils.GetGameBitmap
import com.lex.vrpquest.Utils.RoundByteValue
import com.lex.vrpquest.Utils.SettingGetBoolean
import com.lex.vrpquest.Utils.SettingGetSting
import com.lex.vrpquest.Managers.getFreeStorageSpace
import com.lex.vrpquest.Managers.md5Hash
import com.lex.vrpquest.Utils.LinkStringBuilder
import com.lex.vrpquest.Utils.getDirFullSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun gameInfoPage(game: Game?, OnInstall: (Game) -> Unit, OnClose: () -> Unit) {
    if (game != null) {
        val freespace = getFreeStorageSpace()
        val context = LocalContext.current
        var job:Job = Job()
        var IsEnoughSpace  by remember { mutableStateOf<Boolean?>(null) }
        var IsFTP = SettingGetBoolean(context, "isPrivateFtp") ?: false
        if (IsFTP) {
            LaunchedEffect(true) {
                CoroutineScope(Dispatchers.IO).launch {
                    //check If obb exists
                    val obbfolder = File("/storage/emulated/0/Android/obb/${game.PackageName}")
                    var obbsize = 0L
                    if (obbfolder.exists()) {
                        obbsize = obbfolder.length()
                    }

                    var username = SettingGetSting(context, "username") ?: ""
                    var password = SettingGetSting(context, "pass") ?: ""
                    var host = SettingGetSting(context, "host") ?: ""
                    val client = FTPconnect(username, password, host) ?: return@launch
                    val remoteApk = FTPfindApk(client, "/Quest Games/" + game.ReleaseName + "/")
                    val file = File(remoteApk).name
                    val apksize = client.getSize(file).toLong()
                    IsEnoughSpace = (apksize + (game.Size * 1000000L) - obbsize) < freespace
                    println("apksize: " + apksize)
                    println("gamesize: " + game.Size * 1000000L)
                    println("freespace: " + freespace)
                    client.disconnect()
                }
            }
        } else {
            var tempsize = 0L

            val externalFilesDir = context.getExternalFilesDir(null)?.absolutePath.toString()
            val gamehash =  md5Hash(game.ReleaseName + "\n")

            val hashfolder = File("$externalFilesDir/$gamehash/")
            val extractFolder = File("$externalFilesDir/${game.ReleaseName}/")

            if (hashfolder.exists()) { tempsize += getDirFullSize(hashfolder) }
            if (extractFolder.exists()) { tempsize += getDirFullSize(extractFolder) }

            IsEnoughSpace = ((game.Size * 1000000L) - tempsize) < freespace
            println("tempsize : $tempsize")
            println("isenoughspace : $IsEnoughSpace")
        }

        Box(modifier = Modifier
            .fillMaxSize()
            .background(color = CustomColorScheme.background)
            .pointerInput(Unit) {}) {
            Box(modifier = Modifier
                .padding(10.dp)
                .width(600.dp)
                .clip(RoundedCornerShape(50.dp))
                .align(Alignment.Center)
                .background(
                    CustomColorScheme.surface
                )) {
                Column(
                    Modifier
                        .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                    Row() {
                        Image(modifier = Modifier
                            .clip(RoundedCornerShape(40.dp))
                            .height(100.dp)
                            .clip(RoundedCornerShape(40.dp)),
                            bitmap = GetGameBitmap(game.Thumbnail),
                            //contentScale = ContentScale.Crop,
                            contentDescription = "")

                        Spacer(modifier = Modifier.size(10.dp))
                        Text(modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .weight(0.8f),
                            text = game.GameName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 30.sp,
                            textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.size(10.dp))
                    }
                    Spacer(modifier = Modifier.size(10.dp))

                    Row ( if (game.notes != "") Modifier.height(200.dp) else Modifier
                    ) {
                        Text(
                            modifier = Modifier
                                .weight(0.5f),
                            lineHeight = 27.sp,
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Release Name: ")
                                }
                                append(game.ReleaseName)
    
                                append("\n")
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Version Code: ")
                                }
                                append(game.VersionCode)
                                append("\n")
    
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Last Updated: ")
                                }
                                append(game.LastUpdated)
                                append("\n")
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("Size: ")
                                }
                                append(RoundByteValue(game.Size))
                            }
                        )
                        if(game.notes != "") {
                            Spacer(modifier = Modifier.size(10.dp))
                            Column(modifier = Modifier
                                .weight(0.5F).verticalScroll(rememberScrollState())
                            )  {
                                Text(text = LinkStringBuilder(game.notes))
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.size(10.dp))
                    
                    Row(Modifier.padding(10.dp, 0.dp)) {
                        if (IsEnoughSpace == true || IsEnoughSpace == null) {
                            Button(
                                modifier = Modifier
                                    .weight(0.1F)
                                    .height(50.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CustomColorScheme.tertiary,
                                    contentColor = CustomColorScheme.onSurface
                                ),
                                onClick = { OnInstall(game) },
                            ) { Text(text =  if (IsEnoughSpace == null) "Checking if theres enough space" else "INSTALL") }
                        } else {
                            Button(
                                modifier = Modifier

                                    .weight(0.1F)
                                    .height(50.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CustomColorScheme.error,
                                    contentColor = CustomColorScheme.onSurface
                                ),
                                onClick = {},
                            ) { Text(text = "NO SPACE") }
                        }

                        Spacer(modifier = Modifier.size(10.dp))
                        Button(
                            modifier = Modifier
                                .weight(0.1F)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CustomColorScheme.error,
                                contentColor = CustomColorScheme.onSurface
                            ),
                            onClick = { OnClose() },
                        ) { Text(text = "CLOSE") }

                    }
                    Spacer(modifier = Modifier.size(10.dp))
                }
            }
        }
    }
}