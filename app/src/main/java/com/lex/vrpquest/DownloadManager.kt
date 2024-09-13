package com.lex.vrpquest

import android.content.Context
import android.os.Environment
import android.os.StatFs
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import org.json.JSONObject
import java.io.File
import java.math.BigInteger
import java.net.URL
import java.security.MessageDigest
import java.util.Locale
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.random.Random

class QeueGame(
    val game:Game,
    var MainProgress: MutableState<Float> = mutableStateOf(0.0f),
    var progressList: SnapshotStateList<Float> = mutableStateListOf<Float>(),
    var speed: MutableState<Int> = mutableStateOf(0),
    var state: MutableState<Int> = mutableStateOf(0),
    var IsActive: MutableState<Boolean> = mutableStateOf(false),
    var IsClosing: MutableState<Boolean> = mutableStateOf(false)
)

fun md5Hash(str: String): String {
    val md = MessageDigest.getInstance("MD5")
    val bigInt = BigInteger(1, md.digest(str.toByteArray(Charsets.UTF_8)))
    return String.format("%032x", bigInt)
}

fun findHighestNumberedFile(htmlText: String): Int {
    val regex = """<a href="[^"]*\.7z\.(\d{3})">[^<]*</a>""".toRegex()
    val matches = regex.findAll(htmlText)
    var highestNumber = -1
    for (match in matches) {
        val fileNumber = match.groupValues[1].toInt()
        if (fileNumber > highestNumber) {
            highestNumber = fileNumber
        }
    }
    return highestNumber
}
fun getFreeStorageSpace(): Long {
    val statFs = StatFs(Environment.getExternalStorageDirectory().path)
    val blockSize = statFs.blockSizeLong
    val availableBlocks = statFs.availableBlocksLong
    val freeSpace = blockSize * availableBlocks
    return freeSpace
}

fun RemoveQueueGame(index:Int, gamelist:MutableList<QeueGame>) {
    println("index is : $index")
    if (index == 0) { gamelist[0].IsClosing.value = true } else
        gamelist.removeAt(index)
}

@OptIn(ExperimentalEncodingApi::class)
fun Startinstall(context: Context, gamelist:MutableList<QeueGame>) {
    val externalFilesDir = context.getExternalFilesDir(null)?.absolutePath.toString()
    val dispatch = CoroutineScope(Dispatchers.IO)

    dispatch.launch(Dispatchers.IO) {
        val game = gamelist.firstOrNull()
        if (game != null && !game.IsActive.value) {
            game.IsActive.value = true
            val gamehash =  md5Hash(gamelist[0].game.ReleaseName + "\n")
            val testjson = JSONObject(URL("https://raw.githubusercontent.com/vrpyou/quest/main/vrp-public.json").readText())
            val baseUri = testjson.getString("baseUri")
            val password = String(Base64.decode(testjson.getString("password")))

            val zipCount = findHighestNumberedFile(getUrlFileList("$baseUri/$gamehash/",  "rclone/v69"))
            game.progressList.addAll(List(zipCount) { 0.0f })

            if (File("$externalFilesDir/$gamehash/").exists()) {
                //File("$externalFilesDir/$gamehash/").deleteRecursively()
            }
            if (!File("$externalFilesDir/$gamehash/").exists()) {
                File("$externalFilesDir/$gamehash/").mkdirs()
            }

            //STARTING QUEUED ZIP DOWNLOAD

            var ZipCounter = 0
            var ZipFinished = 0
            var ZipLimit = 4
            for (i in 1..zipCount) {
                println("Downloading zip, $i")

                val formatedNum = String.format(Locale.getDefault(), "%03d", i)

                dispatch.launch(Dispatchers.IO) {
                    println("start download loop")
                    while (ZipCounter >= ZipLimit || !(ZipFinished + ZipLimit  >= i)) {
                        delay(1)
                        if(game.IsClosing.value) {
                            println("cancel download loop")
                            cancel("IsClosing")}
                    }
                    println("didnt cancel download loop, continuing")
                    ZipCounter ++

                    val zipfile = File("$externalFilesDir/$gamehash/$gamehash.7z.$formatedNum")
                    if(zipfile.exists() &&
                        getUrlFileSize("$baseUri/$gamehash/$gamehash.7z.$formatedNum", "rclone/v69") == zipfile.length()) {
                        println("file already exists")
                        ZipCounter--
                        ZipFinished ++
                        game.progressList.set(i-1, 1.0F)
                        cancel("IsClosing")
                    } else {
                        if (zipfile.exists())  { zipfile.delete() }
                        downloadFile("$baseUri/$gamehash/$gamehash.7z.$formatedNum",
                            "$externalFilesDir/$gamehash/$gamehash.7z.$formatedNum",
                            "rclone/v69",
                            { game.progressList.set(i-1, it) }, game.IsClosing
                        )
                        ZipCounter--
                        ZipFinished ++
                    }

                }
            }
            val progressNzip = dispatch.launch(Dispatchers.IO) {
                val num = game.progressList.indices
                var counter = 0F
                while (game.IsActive.value && !game.IsClosing.value) {

                    counter = 0F
                    for (i in game.progressList) {
                        counter += i
                    }
                    game.MainProgress.value = counter / (num.last + 1)

                    if (game.MainProgress.value == 1.0F) {
                        game.state.value = 1

                        val extractDir = File("$externalFilesDir/$gamehash/extract/")
                        if (extractDir.exists()) {extractDir.deleteRecursively()}

                        SevenZipExtract("$externalFilesDir/$gamehash/$gamehash.7z.001",
                            "$externalFilesDir/$gamehash/extract/",
                            false,
                            password,
                            {game.MainProgress.value = it},
                            game.IsClosing)
                        break
                    }

                    delay(10)
                }
                game.IsClosing.value = true
            }

            dispatch.launch(Dispatchers.IO) {
                while (!game.IsClosing.value) {
                    delay(1)

                }

                println("CANCELLING EVERYTHING ON DOWNLOAD COROUTINE")

                //CLEANUP
                if (File("$externalFilesDir/$gamehash/").exists()) {
                    //File("$externalFilesDir/$gamehash/").deleteRecursively()
                }
                game.IsClosing.value = true

                progressNzip.cancelChildren()

                gamelist.removeAt(0)

                if (!gamelist.isEmpty()) {Startinstall(context, gamelist)}
                cancel()
            }
            //increment({gamelist[0].MainProgress.value = it})
            //if (!gamelist[0].MainProgress.value.isNaN()) { gamelist.removeAt(0)break }
            //while ((gamelist[0].MainProgress.value ?: 1.0F) != 1.0F) { delay(10) }
        }
    }
}

fun increment(increment: (Float) -> Unit) {
    val startTime = System.currentTimeMillis()
    val targetTime = 60000 // 1 minute in milliseconds
    fun calculateIncrementValue(elapsedTime: Long): Float {
        val progress = elapsedTime.toFloat() / targetTime.toFloat()
        return progress
    }
    GlobalScope.launch() {
        while (System.currentTimeMillis() - startTime < targetTime) {
            val elapsedTime = System.currentTimeMillis() - startTime
            val incrementValue = calculateIncrementValue(elapsedTime)
            increment(incrementValue)
            Thread.sleep(1)
        }
        increment(1.0F)
    }
}

//fun gameInList(Queuelist: MutableList<QeueGame>, game: Game)