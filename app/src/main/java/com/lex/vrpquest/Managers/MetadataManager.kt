package com.lex.vrpquest.Managers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.ui.graphics.toArgb
import com.lex.vrpquest.CustomColorScheme
import com.lex.vrpquest.Utils.SettingGetSting
import com.lex.vrpquest.Utils.SevenZipExtract
import com.lex.vrpquest.Utils.downloadFile
import com.lex.vrpquest.Utils.showNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.net.URL
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi


class Game(
    val GameName:String,
    val ReleaseName:String,
    val PackageName:String,
    val VersionCode:String,
    val LastUpdated:String,
    val Thumbnail: String,
    val Size:Int,
    var IsInstalled:Boolean,
)

@OptIn(ExperimentalEncodingApi::class)
fun MetadataInitialize(context:Context, state: (Int) -> Unit, progress: (Float) -> Unit, metadata: (ArrayList<Game>) -> Unit) {

        Log.i(context.applicationInfo.name, "Downloading vrp-public.json")
        val externalFilesDir = context.getExternalFilesDir(null)?.absolutePath.toString()
        if (File("$externalFilesDir/meta.7z").exists()) { File("$externalFilesDir/meta.7z").delete()}
        if (File("$externalFilesDir/meta").exists()) { File("$externalFilesDir/meta").deleteRecursively()}
        Log.i(context.applicationInfo.name, "Downloading meta.7z")

        var testjson = JSONObject()

        try {
            testjson = JSONObject(URL("https://raw.githubusercontent.com/vrpyou/quest/main/vrp-public.json").readText())
        } catch (E:Exception) {
            disableSSLCertificateChecking()
            testjson = JSONObject(URL("https://vrpirates.wiki/downloads/vrp-public.json").readText())
            enableSSLCertificateChecking()
        }

        val baseUri = testjson.getString("baseUri")
        val password = String(Base64.decode(testjson.getString("password")))
        state(0)
        println("$baseUri" + "meta.7z")
        downloadFile("$baseUri" + "meta.7z", "$externalFilesDir/meta.7z","rclone/v69", { progress(it) }).toString()
        state(1)
        SevenZipExtract("$externalFilesDir/meta.7z", "$externalFilesDir/meta/", true, password, { progress(it) });
        state(2)
        metadata(SortGameList(context, { progress(it) }))
        state(3)
        //zip(File("$externalFilesDir/meta"), File("$externalFilesDir/meta.zip"))
}
@OptIn(ExperimentalEncodingApi::class)
fun MetadataInitializeFTP(context:Context, state: (Int) -> Unit, progress: (Float) -> Unit, metadata: (ArrayList<Game>) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch(Dispatchers.IO) {
        try {
            Log.i(context.applicationInfo.name, "Downloading vrp-public.json")
            val externalFilesDir = context.getExternalFilesDir(null)?.absolutePath.toString()
            if (File("$externalFilesDir/meta.7z").exists()) { File("$externalFilesDir/meta.7z").delete()}
            if (File("$externalFilesDir/meta").exists()) { File("$externalFilesDir/meta").deleteRecursively()}
            Log.i(context.applicationInfo.name, "Downloading meta")

            var username = SettingGetSting(context, "username") ?: ""
            var password = SettingGetSting(context, "pass") ?: ""
            var host = SettingGetSting(context, "host") ?: ""

            val client = FTPconnect(username, password, host) ?: return@launch

            state(0)

            //FTPdownloadFile(client, "$externalFilesDir/meta/VRP-GameList.txt", "/Quest Games/VRP-GameList.txt")
            //FTPdownloadRecursive(client, "$externalFilesDir/test", "/Quest Games/10 Seconds v3+1.0 -VRP")
            //downloadFile("$baseUri" + "meta.7z", "$externalFilesDir/meta.7z","rclone/v69", { progress(it) }).toString()
            //zip(File("$externalFilesDir/meta"), File("$externalFilesDir/meta.zip"))
            println("FTP metadata download")
            FTPdownloadFile(client, "$externalFilesDir/meta.7z", "/Quest Games/meta.7z", { progress(it) })

            state(1)
            SevenZipExtract("$externalFilesDir/meta.7z", "$externalFilesDir/meta/", true, "", { progress(it) });

            delay(100)

            state(2)
            metadata(SortGameList(context, { progress(it) }))

            state(3)
        } catch (e: Exception) {
            showNotification(context, "METADAT INSTALL FAILED")
        }
    }
}


fun SortGameList(context: Context, progress: (Float) -> Unit):ArrayList<Game> {
    val externalFilesDir = context.getExternalFilesDir(null)?.absolutePath.toString()
    var gamelist:ArrayList<Game> =  ArrayList<Game>()

    val pfile = File("$externalFilesDir/meta/VRP-GameList.txt")
    if (pfile.exists()) {
        val gamedata = pfile.readText().drop(pfile.readText().indexOf("\n") + 1).split("\n")
        val gamesize = gamedata.count()
        val EmptyThumb:Bitmap = Bitmap.createBitmap(374, 214, Bitmap.Config.ARGB_8888)
        EmptyThumb.eraseColor(CustomColorScheme.background.toArgb())
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        gamedata.forEachIndexed  { index, value ->
            var tempvalue = value.split(";")
            if (tempvalue.size > 4) {
                val thumbnail = "${externalFilesDir}/meta/.meta/thumbnails/${tempvalue[2]}.jpg"
                gamelist.add(
                    Game(
                    GameName = tempvalue[0],
                    ReleaseName = tempvalue[1],
                    PackageName = tempvalue[2],
                    VersionCode = tempvalue[3],
                    LastUpdated = tempvalue[4],
                    Size = tempvalue[5].toInt(),
                    Thumbnail = if (File(thumbnail).exists()) thumbnail else "",
                    IsInstalled = false
                )
                )
            }
            progress((index + 1F) / gamesize)
        }
        progress(1F)
        return gamelist
    }
    return gamelist
}

