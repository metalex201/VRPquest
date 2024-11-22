package com.lex.vrpquest.Pages

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lex.vrpquest.CustomColorScheme
import com.lex.vrpquest.Utils.DatastoreTextSwitch
import com.lex.vrpquest.Utils.IsShizukuGranted
import com.lex.vrpquest.MainActivity
import com.lex.vrpquest.Utils.RoundDivider
import com.lex.vrpquest.Utils.SettingGetBoolean
import com.lex.vrpquest.Utils.SettingGetSting
import com.lex.vrpquest.Utils.SettingStoreBoolean
import com.lex.vrpquest.Utils.SettingStoreStringSet
import com.lex.vrpquest.Utils.SettingsTextButton
import com.lex.vrpquest.Utils.SettingsTextField
import com.lex.vrpquest.Utils.checkPermission
import com.lex.vrpquest.Utils.isPackageInstalled
import com.lex.vrpquest.Utils.settingsGroup
import com.lex.vrpquest.Utils.settingsText
import com.lex.vrpquest.Managers.testfpt
import com.lex.vrpquest.Utils.SettingGetStringSet
import com.lex.vrpquest.Utils.SettingStoreString
import com.lex.vrpquest.Utils.SettingsTextDropdown
import com.lex.vrpquest.Utils.SettingsTextSwitch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku
import java.io.File

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun SettingsPage(updatePage: () -> Unit) {
    // Retrieve the context from Compose
    val context = LocalContext.current
    val activity = (LocalContext.current as Activity)

    var ShizukuExists by remember { mutableStateOf(isPackageInstalled(context, "moe.shizuku.privileged.api")) }
    var isBinder by remember { mutableStateOf(Shizuku.pingBinder()) }
    var isGranted by remember { mutableStateOf(false) }

    LaunchedEffect(!isBinder || !isGranted || !ShizukuExists) {
        while(!isBinder || !isGranted || !ShizukuExists) {
            delay(100)
            ShizukuExists = isPackageInstalled(context, "moe.shizuku.privileged.api")
            isBinder = Shizuku.pingBinder()
            if(isBinder) {
                isGranted = IsShizukuGranted()
            }
        }
    }

    val scrollState = rememberScrollState()
    var scrollfloat by remember { mutableStateOf(scrollState.value.toFloat()) }

    LaunchedEffect(scrollState) {
        GlobalScope.launch() {
            while (true) {
                if (scrollState.value > 100) {scrollfloat = 100F} else {
                    scrollfloat = scrollState.value.toFloat()
                }
            }
        }
    }

    Column(modifier = Modifier
        .graphicsLayer { alpha = 0.99f }
        .drawWithContent {
            val colors = listOf(
                Color.Transparent,
                Color.Black)
            drawContent()
            drawRect(
                brush = Brush.verticalGradient(
                    colors, startY = 0f,
                    endY = scrollfloat
                ),
                blendMode = BlendMode.DstIn) }
        .verticalScroll(scrollState)
        .fillMaxWidth()
        .padding(10.dp)
    ) {

        //SHIZUKU STATE INDICATOR
        settingsGroup() {
            if(ShizukuExists) {
                if (isBinder) { //shizuku is running
                    if (!isGranted) {
                        SettingsTextButton("Shizuku is running but permission has not been granted",
                            "Request Permission", { checkPermission(0) })
                    } else {
                        settingsText("Shizuku is running and is connected")
                    }
                } else {
                    settingsText("Shizuku is not running")
                }
            } else {
                settingsText("Shizuku needs to be installed")
            }
        }

        //SHIZUKU PAIR INSTRUCTIONS
        if (!(ShizukuExists && Shizuku.pingBinder() && IsShizukuGranted())) {
            settingsGroup() {
                settingsText("This application can use ADB commands for installing games in the background without any user interaction and a small number of applications require it for proper installation, " +
                        "for this to be possible we use an app called shizuku, theres currently two version that can be used, the Original Shizuku, " +
                        "and a modified version made for quest named shizuku4quest, both can be found in the VRP library")

                RoundDivider()

                settingsText("Shizuku4quest setup:\n\n First install Shizuku4quest from the library, " +
                        "then open the app and follow the instructions in the app, after you finished whit the setup, make sure to allow VRPquest to use Shizuku")

                RoundDivider()

                SettingsTextButton("Original Shizuku setup:\n\n First install Shizuku from the library, " +
                        "then open the app tap on pair and follow these instructions on setting up wireless debugging:\n \n" +

                        "(Incase developer options isn't enabled then the device info settings will be opened first, scroll down and tap on build number 7 times, then open developer settins again) \n \n" +

                        "1. Open Developer Settings on your device. \n" +
                        "2. Find and tap on 'Wireless Debugging'.\n" +
                        "3. Tap the left side of 'Wireless Debugging' to access hidden settings.\n" +
                        "4. Select 'Pair device with pairing code'.\n" +
                        "5. Follow the on-screen instructions to complete pairing.\n",

                    "Open Developer Settings"
                ) {
                    CoroutineScope(Dispatchers.IO).launch {
                        var intent = Intent("com.android.settings.APPLICATION_DEVELOPMENT_SETTINGS")


                        if (Settings.Secure.getInt(context.contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) == 0 ) {
                            intent = Intent().setClassName(
                                "com.android.settings",
                                "com.android.settings.Settings\$MyDeviceInfoActivity"
                            )
                        }

                        CoroutineScope(Dispatchers.IO).launch { activity.finishAndRemoveTask() }

                        delay(200)


                        intent.setFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK or
                                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                        )
                        context.startActivity(intent)
                        delay(200)

                        val intentt = Intent(context, MainActivity::class.java)
                        intentt.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        intentt.putExtra("page", 3)
                        context.startActivity(intentt)
                    }
                }
                //group end
            }
        }

        //PRIVATE MIRROR SERVER
        settingsGroup() {
            var buttonText by remember { mutableStateOf("if a connection can be made, it will be saved") }


            var username by remember { mutableStateOf(SettingGetSting(context, "username") ?: "") }
            var password by remember { mutableStateOf(SettingGetSting(context, "pass") ?: "") }
            var host by remember { mutableStateOf(SettingGetSting(context, "host") ?: "") }

            var IsFTP by remember { mutableStateOf(SettingGetBoolean(context, "isPrivateFtp") ?: false) }

            LaunchedEffect(true) {
                CoroutineScope(Dispatchers.IO).launch {
                    IsFTP = testfpt(username, password, host)
                }
            }

            settingsText("This application supports paid private FTP mirrors, an advantage of this is that the files hosted on ftp are not compressed," +
                    " so you only need to have enough space for the game instead of 2x the storage space for installing the archives and unpacking them from the main VRP server")

            SettingsTextField("USERNAME", username, "USERNAME", {username = it})
            SettingsTextField("PASSWORD", password, "PASSWORD", {password = it})
            SettingsTextField("HOST", host, "HOST", {host = it})
            if (IsFTP) {
                SettingsTextButton("Disconnect from the FTP server", "DISCONNECT") {
                    SettingStoreBoolean(context, "isPrivateFtp", false)
                    SettingStoreString(context, "username", "")
                    SettingStoreString(context, "pass", "")
                    SettingStoreString(context, "host", "")
                }
            } else {
                SettingsTextButton( buttonText, "CONNECT") {
                    GlobalScope.launch {
                        IsFTP = testfpt(username, password, host)
                        if (IsFTP == true) {
                            SettingStoreBoolean(context, "isPrivateFtp", true)
                            SettingStoreString(context, "username", username)
                            SettingStoreString(context, "pass", password)
                            SettingStoreString(context, "host", host)
                        } else {
                            buttonText = "Connection failed, check if everything is written correctly and try again"
                        }
                    }
                }
            }
        }


        settingsGroup() {
            settingsText("Download Settings")
            RoundDivider()
            SettingsTextButton(text = "Delete cache (you should not have any installs in progress)", buttontext = "Delete") {
                var localfile = File("/storage/emulated/0/Android/data/com.lex.vrpquest/files/")
                if (localfile.exists()) {
                    localfile.deleteRecursively()
                }
            }

            SettingsTextButton(text = "Clear ignored game donations blacklist", buttontext = "Clear") {
                SettingStoreStringSet(context, "DonateBlacklist", setOf())
            }

            DatastoreTextSwitch(context, "Delete half-finished games when they're re-downloaded", id = "UnfinishedDelete")

        }
        settingsGroup() {
            settingsText("Shizuku Settings")
            RoundDivider()
            DatastoreTextSwitch(context, "Stop device from going to sleep while there is a download in progress", id = "ShizukuAvoidSleep")
        }

        //UI SETTINGS

        settingsGroup() {

            settingsText("UI Settings")

            RoundDivider()


            SettingsTextDropdown("Change the theme (requires restart) ",
                SettingGetSting(context, "theme") ?: "dark",
                listOf("dark", "light", "transparent"), {
                    SettingStoreString(context, "theme", it)
                    updatePage()
                })
        }

        // VERSION INDICATOR
        settingsGroup() {
            val context = LocalContext.current
            val packageInfo = remember {
                context.packageManager.getPackageInfo(context.packageName, 0)
            }

            val versionCode =
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    packageInfo.longVersionCode
                } else {
                    packageInfo.versionCode.toLong()
                }

            settingsText("VRPquest v${packageInfo.versionName} - Build $versionCode")
        }
    }
}


