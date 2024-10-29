package com.lex.vrpquest.Pages

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
                    Text(text = "release Name: " + game.ReleaseName)
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(text = "Version Code: " + game.VersionCode)
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(text = "Last Updated: " + game.LastUpdated)
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(text = "size: " + RoundByteValue(game.Size))
                    Spacer(modifier = Modifier.size(10.dp))
                    if (IsEnoughSpace == null) {
                        Text(text = "Checking if theres enough space")
                        Spacer(modifier = Modifier.size(10.dp))
                    }

                    Row(Modifier.padding(10.dp, 0.dp)) {
                        if (IsEnoughSpace == true || IsEnoughSpace == null) {
                            Button(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(0.1F)
                                    .height(50.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CustomColorScheme.tertiary,
                                    contentColor = CustomColorScheme.onSurface
                                ),
                                onClick = { OnInstall(game) },
                            ) { Text(text = "INSTALL") }
                        } else {
                            Button(
                                modifier = Modifier
                                    .fillMaxWidth()
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
                                .fillMaxWidth()
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