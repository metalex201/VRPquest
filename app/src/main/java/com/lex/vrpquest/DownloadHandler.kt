package com.lex.vrpquest

import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.ConnectionPool
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.buffer
import okio.sink
import org.json.JSONObject
import java.io.File
import java.net.URL
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.math.sin


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
    GlobalScope.launch {
         if (1 == 0) {
            Log.i(context.applicationInfo.name, "Downloading vrp-public.json")
            val externalFilesDir = context.getExternalFilesDir(null)?.absolutePath.toString()
            if (File("$externalFilesDir/meta.7z").exists()) { File("$externalFilesDir/meta.7z").delete()}
            if (File("$externalFilesDir/meta").exists()) { File("$externalFilesDir/meta").deleteRecursively()}
            Log.i(context.applicationInfo.name, "Downloading meta.7z")
            val testjson = JSONObject(URL("https://raw.githubusercontent.com/vrpyou/quest/main/vrp-public.json").readText())
            val baseUri = testjson.getString("baseUri")
            val password = String(Base64.decode(testjson.getString("password")))

            state(0)
            downloadFile("$baseUri" + "meta.7z", "$externalFilesDir/meta.7z","rclone/v69", { progress(it) }).toString()
            state(1)
            SevenZipExtract("$externalFilesDir/meta.7z", "$externalFilesDir/meta/", true, password, { progress(it) });
        }
        delay(50)
        state(2)
        metadata(SortGameList(context, { progress(it) }))
        state(3)
        //zip(File("$externalFilesDir/meta"), File("$externalFilesDir/meta.zip"))
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
                gamelist.add(Game(
                    GameName = tempvalue[0],
                    ReleaseName = tempvalue[1],
                    PackageName = tempvalue[2],
                    VersionCode = tempvalue[3],
                    LastUpdated = tempvalue[4],
                    Size = tempvalue[5].toInt(),
                    Thumbnail = if (File(thumbnail).exists()) thumbnail else "",
                    IsInstalled = false
                ))
            }
            progress((index + 1F) / gamesize)
        }
        progress(1F)
        return gamelist
    }
    return gamelist
}

fun downloadFile(url: String, destinationPath: String, customHeader: String, progress: (Float) -> Unit, IsStopping:MutableState<Boolean> = mutableStateOf(false)): Boolean {
    println("$url START")
    val client = OkHttpClient.Builder()
        .addNetworkInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", customHeader)
                .url(url)
                .build()
            chain.proceed(request)
        }
        .build()
    println("REQUEST $url")
    val request = Request.Builder().url(url).build()
    val response = client.newCall(request).execute()
    Log.i(TAG, response.toString())
    if (!response.isSuccessful) {
        println("Download failed: ${response.code}")
        return false
    }
    println("BODY $url ")

    val file = File(destinationPath)
    if (file.parentFile!!.exists()) {
        file.parentFile!!.mkdirs()
    }

    val sourceBytes = response.body!!.source()
    val sink = file.sink().buffer()
    println("DOWNLOAD $url ")
    val contentLength = response.body!!.contentLength()
    var totalRead: Long = 0
    var lastRead: Long
    var count: Long = 0
    while (sourceBytes
            .read(sink.buffer, 8L * 1024)
            .also { lastRead = it } != -1L && !IsStopping.value
    ) {

        totalRead += lastRead
        sink.emitCompleteSegments()

        count++
        //only report progress every 100th write, to be more efficient
        if (count % 100 == 0L) {
            progress((((totalRead) * 100L) / contentLength) / 100F)
        }
    }

    var totalBytesRead = 0L

    while (true) {
        if(IsStopping.value) { break }

        val readCount = response.body!!.source().read(sink.buffer,8192L)
        if (readCount == -1L) break
        totalBytesRead += readCount
    }

    progress(1.0F)
    sink.close()
    response.body!!.close()

    println("Download finished: $destinationPath")
    return true
}


fun getUrlFileList(url: String, customHeader: String): String {
    val client = OkHttpClient.Builder()
        .addNetworkInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", customHeader)
                .url(url)
                .build()
            chain.proceed(request)
        }
        .build()

    val request = Request.Builder().url(url).build()
    val response = client.newCall(request).execute()
    Log.i(TAG, response.toString())
    if (!response.isSuccessful) {
        println("Download failed: ${response.code}")
        return ""
    }

    val result = response.body!!.string()
    response.body!!.close()
    println(result)
    return result
}

fun getUrlFileSize(url: String, customHeader: String): Long {
    val client = OkHttpClient.Builder()
        .addNetworkInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", customHeader)
                .url(url)
                .build()
            chain.proceed(request)
        }
        .build()

    val request = Request.Builder().url(url).build()
    val response = client.newCall(request).execute()
    Log.i(TAG, response.toString())
    if (!response.isSuccessful) {
        println("Download failed: ${response.code}")
        return 0
    }

    val result = response.body!!.contentLength()
    response.body!!.close()
    return result
}

fun getUrlFileSizeDir(url: String, customHeader: String): List<String>  {
    val client = OkHttpClient.Builder()
        .addNetworkInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", customHeader)
                .url(url)
                .build()
            chain.proceed(request)
        }
        .build()

    val request = Request.Builder().url(url).build()
    val response = client.newCall(request).execute()
    Log.i(TAG, response.toString())
    if (!response.isSuccessful) {
        println("Download failed: ${response.code}")
        return listOf()
    }

    val result = response.body!!.string()
    response.body!!.close()

    println(result)
    val regex = "<a href=\"([^\"]+)\">[^<]+</a>".toRegex()
    val matches = regex.findAll(result)

    return matches.drop(1).map { it.groupValues[1] }.toList()
}

