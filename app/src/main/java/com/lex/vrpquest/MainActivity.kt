package com.lex.vrpquest


import android.content.Context
import android.os.Build
import android.os.Bundle
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import rikka.shizuku.Shizuku
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.lex.vrpquest.Managers.DonateGame
import com.lex.vrpquest.Managers.DonateQueue
import com.lex.vrpquest.Managers.StartDonation
import com.lex.vrpquest.Managers.decrypt
import com.lex.vrpquest.Managers.getDonateGames
import com.lex.vrpquest.Managers.Game
import com.lex.vrpquest.Managers.QueueGame
import com.lex.vrpquest.Managers.SortGameList
import com.lex.vrpquest.Managers.Startinstall
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
import com.lex.vrpquest.Utils.SettingGetStringSet
import com.lex.vrpquest.Utils.TextBar
import com.lex.vrpquest.Utils.decryptPassword
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

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
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        //Shizuku.addRequestPermissionResultListener(REQUEST_PERMISSION_RESULT_LISTENER);
        //showNotification(applicationContext, "TEST NOTIF")
        //checkPermission(0)
        //enableEdgeToEdge()

        //settingsTest
        //SettingStoreSting(applicationContext, "test", "TESTTESTTEST")
        //println("SETTINGS STORE GET RESULT : " + SettingGetSting(applicationContext, "test"))
        //SettingStoreBoolean(applicationContext, "test", true)
        //println("SETTINGS STORE GET RESULT : " + SettingGetSting(applicationContext, "test"))

        //OBB TEST
        //println("OBBTEST")
        //val res = File("/storage/emulated/0/Android/obb/TESTTT").mkdirs()
        //println("OBBTEST: $res")
        //println(ShizAdbCommand("echo testt"))

        val inputText = "hAG8Jd8Qdmlffltz-rGi8lasjOvwRuEIt0jKuA                          "
        //val inputText = "hAG8Jd8Qdmlffltz-rGi8lasjOvwRuEIt0jKuA                          "

        println("HEX SIZE " + inputText.toByteArray().size)

        val code = listOf(0x9c, 0x93, 0x5b, 0x48, 0x73, 0x0a, 0x55, 0x4d,
            0x6b, 0xfd, 0x7c, 0x63, 0xc8, 0x86, 0xa9, 0x2b,
            0xd3, 0x90, 0x19, 0x8e, 0xb8, 0x12, 0x8a, 0xfb,
            0xf4, 0xde, 0x16, 0x2b, 0x8b, 0x95, 0xf6, 0x38)
            .map { it.toByte() }.toByteArray()

        val plainText = decrypt(inputText.toByteArray(), code)
        println("CODE RESULT: \n $plainText")








        val encryptedPassword = "hAG8Jd8Qdmlffltz-rGi8lasjOvwRuEIt0jKuA"
        val decryptedPassword = decryptPassword(encryptedPassword)

        if (decryptedPassword != null) {
            println("Decrypted password: $decryptedPassword")
        } else {
            println("Decryption failed")
        }

        setContent {
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
            var IsShizukuConnected by remember { mutableStateOf(Shizuku.pingBinder() && com.lex.vrpquest.Utils.checkPermission(
                0
            )
            ) }    // either shizuku doesnt exist or the app doesnt have permission

            var searchText by remember { mutableStateOf("") }

            MaterialTheme(colorScheme = CustomColorScheme) {
                Surface(modifier = Modifier
                    .fillMaxSize()
                    .scale(1F), color = CustomColorScheme.background) {
                    LaunchedEffect(Queuelist.isEmpty()) {
                        IsQueuelistEmpty = Queuelist.isNotEmpty()
                        DonateQueueList.isNotEmpty()
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

                                    when(Page) {
                                        0 -> {
                                            SearchBar(Modifier.weight(0.1f), searchText, {searchText = it}, "Search here")
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
                                .padding(10.dp, 0.dp)) {

                                if(Gamelist.isEmpty()) {
                                    FullText("Game List is currently empty, gamedata is either  deleted or there was an issue connecting to the VRP server")
                                } else {  MainPage(Gamelist, searchText, {GameInfo = it}) } //MAINPAGE ALWAYS COMPOSED

                                if (Page != 0) { // OVERLAY OVER MAINPAGE
                                    Box(modifier = Modifier
                                        .fillMaxSize()
                                        .background(color = CustomColorScheme.background)
                                        .pointerInput(Unit) {}) { // AVOID PASSING CLICKS THROUGH TO MAINPAGE
                                        when(Page) {
                                            1 -> {
                                                if(Queuelist.isEmpty() && DonateQueueList.isEmpty()) {
                                                    FullText("Queue List is currently empty, you can start an install through tapping on an app and tapping on install")
                                                } else { QueuePage(Queuelist, DonateQueueList)}
                                            }

                                            3 -> {SettingsPage()}
                                        }
                                    }
                                }
                            }
                        }
                    }

                    var IsDonated  by remember { mutableStateOf(false) }
                    var donatelist= remember {mutableListOf<DonateGame>()}

                    if (Page == 2)
                        LoadPage({Page = 0
                            getDonateGames(applicationContext, Gamelist).forEach() { donatelist.add(it) }
                            if (!(donatelist.size == 0)) {
                                IsDonated = true
                            }}, //avoid loading donatelist if its empty
                            {Gamelist = it.toMutableList()}) // FOR LOADING META.7z

                    if (IsDonated) { DonatePage({Page = 0; IsDonated = false}, { it.forEach() {
                        DonateQueueList.add(
                            DonateQueue(packageName = it.packageName,
                           IsActive = mutableStateOf(false))
                        ) }
                           StartDonation(applicationContext, DonateQueueList)
                           }, donatelist) }

                    gameInfoPage(GameInfo, { game ->
                         Queuelist.add(QueueGame(game))
                         Startinstall(applicationContext, Queuelist)
                         GameInfo = null
                         }, {GameInfo = null})

                    PermissionPage()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Shizuku.removeRequestPermissionResultListener(REQUEST_PERMISSION_RESULT_LISTENER)
    }
}

