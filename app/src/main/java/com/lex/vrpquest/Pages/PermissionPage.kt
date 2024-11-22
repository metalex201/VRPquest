package com.lex.vrpquest.Pages

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
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
import androidx.core.app.ActivityCompat
import com.lex.vrpquest.CustomColorScheme
import com.lex.vrpquest.Utils.isPermissionAllowed
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay

@RequiresApi(Build.VERSION_CODES.R)
@Composable
fun PermissionPage(page:Int, changePage:(Int) -> Unit) {
    val context = LocalContext.current
    val activity = (LocalContext.current as Activity)
    var InstallApkPerm by remember { mutableStateOf(context.packageManager.canRequestPackageInstalls()) }
    var FileAccessPerm by remember { mutableStateOf(isPermissionAllowed(context, "android.permission.WRITE_EXTERNAL_STORAGE")) }
    LaunchedEffect(true) {
        while(true) {
            delay(10)
            InstallApkPerm = context.packageManager.canRequestPackageInstalls()
            FileAccessPerm = isPermissionAllowed(context, "android.permission.WRITE_EXTERNAL_STORAGE")
            if (InstallApkPerm && FileAccessPerm) {
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
        if(!FileAccessPerm) {
            ActivityCompat.requestPermissions(activity,
                arrayOf("android.permission.WRITE_EXTERNAL_STORAGE"),
                23)
        }
    }
    if (!(InstallApkPerm && FileAccessPerm)) {
        changePage(5)
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
                    Text(text = "These permissions are required by this application:", color = CustomColorScheme.onSurface)
                    Spacer(modifier = Modifier.size(20.dp))
                    Text(modifier = Modifier.padding(20.dp,0.dp),
                        color = if (InstallApkPerm) CustomColorScheme.tertiary else CustomColorScheme.error,
                        text = "- INSTALL APK")
                    Text(modifier = Modifier.padding(20.dp,0.dp),
                        color = if (FileAccessPerm) CustomColorScheme.tertiary else CustomColorScheme.error,
                        text = "- FILE ACCESS")

                    Spacer(modifier = Modifier.size(20.dp))
                    Row() {

                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(0.1F)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CustomColorScheme.tertiary,
                                contentColor = CustomColorScheme.onSurface
                            ),
                            onClick = { grant() },
                        ) { Text(text = "GRANT") }
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
                            onClick = { System.out.close() },
                        ) { Text(text = "EXIT", color = CustomColorScheme.onSurface) }
                    }
                }
            }
        }
    } else { if (page == 5) {changePage(0)}}
}