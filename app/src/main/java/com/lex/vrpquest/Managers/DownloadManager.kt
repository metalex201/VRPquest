package com.lex.vrpquest.Managers

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import androidx.annotation.RequiresApi
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import com.lex.vrpquest.Utils.SettingGetBoolean
import com.lex.vrpquest.Utils.SettingGetSting
import com.lex.vrpquest.Utils.SevenZipExtract
import com.lex.vrpquest.Utils.ShizAdbCommand
import com.lex.vrpquest.Utils.canUseShizuku
import com.lex.vrpquest.Utils.downloadFile
import com.lex.vrpquest.Utils.getUrlFileList
import com.lex.vrpquest.Utils.getUrlFileSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.math.BigInteger
import java.net.URL
import java.security.MessageDigest
import java.util.Locale
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class QueueGame(
    val game: Game,
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

fun RemoveQueueGame(index:Int, gamelist:MutableList<QueueGame>) {
    println("index is : $index")
    if (index == 0) { gamelist[0].IsClosing.value = true } else {
        gamelist.removeAt(index)
    }
}

@RequiresApi(Build.VERSION_CODES.P)
@OptIn(ExperimentalEncodingApi::class)
fun Startinstall(
    context: Context,
    gamelist: MutableList<QueueGame>
) {
    var IsFTP = SettingGetBoolean(context, "isPrivateFtp") ?: false


    val externalFilesDir = context.getExternalFilesDir(null)?.absolutePath.toString()
    val dispatch = CoroutineScope(Dispatchers.IO)

    dispatch.launch(Dispatchers.IO) {
        val game = gamelist.firstOrNull()
        if (game != null && !game.IsActive.value) {
            game.IsActive.value = true

            //AVOID SLEEP

            var IsStopSleep = SettingGetBoolean(context, "ShizukuAvoidSleep") ?: false

            if(canUseShizuku() && IsStopSleep) {
                ShizAdbCommand("am broadcast -a com.oculus.vrpowermanager.prox_close")
            }

            //PRIVATE MIRROR

            if (IsFTP) {
                var IsFinished = false
                val downloaddir = "$externalFilesDir${game.game.ReleaseName}/"
                val localApk = downloaddir + game.game.PackageName + ".apk"
                val localInstalTXT = downloaddir + "install.txt"

                val localOBB = "/storage/emulated/0/Android/obb/${game.game.PackageName}/"

                dispatch.launch(Dispatchers.IO) {
                    var username = SettingGetSting(context, "username") ?: ""
                    var password = SettingGetSting(context, "pass") ?: ""
                    var host = SettingGetSting(context, "host") ?: ""
                    val client = FTPconnect(username, password, host) ?: return@launch

                    val remoteApk = FTPfindApk(client, "/Quest Games/" + game.game.ReleaseName + "/")
                    val remoteObb = "/Quest Games/" + game.game.ReleaseName + "/" + game.game.PackageName + "/"
                    val remoteTXT = "/Quest Games/" + game.game.ReleaseName + "/" + "install.txt"
                    val remotePath = "/Quest Games/" + game.game.ReleaseName

                    val IsOBB = FTPfileExists(client, remoteObb)

                    val IsDeletePrev = SettingGetBoolean(context, "UnfinishedDelete") ?: false

                    if(IsDeletePrev) {
                        if (File(localOBB).exists()) { File(localOBB).deleteRecursively() }
                        if (File(localApk).exists()) { File(localApk).delete() }
                    }

                    if(FTPfileExists(client, remoteTXT) && canUseShizuku()) {
                        game.state.value = 0
                        FTPdownloadRecursive(client, downloaddir, remotePath, { game.MainProgress.value = it })
                        if(File("$downloaddir/_data.7z").exists()) {
                            println("_DATA.7Z EXISTS EXTRACTING")
                            game.state.value = 1
                            SevenZipExtract("$downloaddir/_data.7z", downloaddir, true, "", { game.MainProgress.value = it });
                        }

                        ParseInstallTXT(context, localInstalTXT)

                        //CLEANUP
                        if (File(downloaddir).exists()) { File(downloaddir).delete() }
                    } else {
                        if(remoteApk != "") {
                            if (IsOBB) {
                                game.state.value = 3
                                FTPdownloadRecursive(client, localOBB, remoteObb, { game.MainProgress.value = it })
                            }
                            game.state.value = 4
                            FTPdownloadFile(client, localApk, remoteApk, { game.MainProgress.value = it })
                            installApk(context, localApk, game.game)
                        }
                    }

                    //differentiate unfinished installs and finished ones
                    IsFinished = true
                }



                dispatch.launch(Dispatchers.IO) {
                    while (!game.IsClosing.value) {
                        if (IsFinished) {
                            game.IsClosing.value = true

                            gamelist.removeAt(0)

                            if (!gamelist.isEmpty()) {
                                Startinstall(context, gamelist)
                            }
                            cancel()
                        }
                        delay(1)
                    }
                    if (!IsFinished) {
                        if (File(localOBB).exists()) { File(localOBB).deleteRecursively() }
                        if (File(localApk).exists()) { File(localApk).delete() }
                        if (File(downloaddir).exists()) { File(downloaddir).delete() }
                        gamelist.removeAt(0)

                        if (!gamelist.isEmpty()) {
                            Startinstall(context, gamelist)
                        } else {

                            //TURN BACK SLEEP SENSOR

                            if(canUseShizuku() && IsStopSleep) {
                                ShizAdbCommand("am broadcast -a com.oculus.vrpowermanager.automation_disable")
                            }
                        }
                        cancel()
                    }

                }
            }

            //PUBLIC MIRROR

            if(!IsFTP) {
                val gamehash =  md5Hash(gamelist[0].game.ReleaseName + "\n")

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
                val hashfolder = "$externalFilesDir/$gamehash/"

                val obbFolder = "$externalFilesDir/${game.game.ReleaseName}/${game.game.ReleaseName}/${game.game.PackageName}"
                val extractFolder = "$externalFilesDir/${game.game.ReleaseName}/"
                val zipCount = findHighestNumberedFile(getUrlFileList("$baseUri/$gamehash/",  "rclone/v69"))
                val IsDeletePrev = SettingGetBoolean(context, "UnfinishedDelete") ?: false

                if(IsDeletePrev) {
                    if (File(hashfolder).exists()) { File(hashfolder).deleteRecursively() }
                    if (File(extractFolder).exists()) { File(extractFolder).delete() }
                }


                if (!(!File(hashfolder).exists() && File(obbFolder).exists())) {

                    game.progressList.addAll(List(zipCount) { 0.0f })

                    if (!File(hashfolder).exists()) {
                        File(hashfolder).mkdirs()
                    }

                    //ONLY START DOWNLOAD IF EXTRACTED FOLDER DOESNT EXIST

                    //STARTING QUEUED ZIP DOWNLOAD
                    var ZipCounter = 0
                    var ZipFinished = 0
                    var ZipLimit = 2
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

                                fun downloadZip() {
                                    try {
                                        downloadFile("$baseUri/$gamehash/$gamehash.7z.$formatedNum",
                                            "$externalFilesDir/$gamehash/$gamehash.7z.$formatedNum",
                                            "rclone/v69",
                                            { game.progressList.set(i-1, it) }, game.IsClosing
                                        )
                                    } catch (e:Exception) {
                                        val zipfile = File("$externalFilesDir/$gamehash/$gamehash.7z.$formatedNum")
                                        if (zipfile.exists()) { zipfile.delete() }
                                        downloadZip()
                                    }
                                }

                                downloadZip()

                                ZipCounter--
                                ZipFinished ++
                            }

                        }
                    }
                } else {
                    //IF ARCHIVE ALREADY EXTRACTED THEN ITS FILES HAVE BEEN INSTALLED
                    game.MainProgress.value = 1.0F
                }


                val progressNzip = dispatch.launch(Dispatchers.IO) {
                    val num = game.progressList.indices
                    var counter = 0F
                    while (game.IsActive.value && !game.IsClosing.value) {
                        if (game.MainProgress.value != 1.0F) {
                            counter = 0F
                            for (i in game.progressList) {
                                counter += i
                            }
                            game.MainProgress.value = counter / (num.last + 1)
                        } else {
                            game.state.value = 1

                            val extractDir = File(extractFolder)
                            game.MainProgress.value = 1F
                            if (extractDir.exists()) {
                                if(File(hashfolder).exists()) {
                                    extractDir.deleteRecursively()

                                    SevenZipExtract("$externalFilesDir/$gamehash/$gamehash.7z.001",
                                        extractFolder,
                                        false,
                                        password,
                                        {game.MainProgress.value = it},
                                        game.IsClosing
                                    )
                                    File(hashfolder).deleteRecursively()
                                }
                            } else  {
                                SevenZipExtract("$externalFilesDir/$gamehash/$gamehash.7z.001",
                                    extractFolder,
                                    false,
                                    password,
                                    {game.MainProgress.value = it},
                                    game.IsClosing
                                )
                                File(hashfolder).deleteRecursively()
                            }

                            if (game.MainProgress.value == 1F) {
                                //move obb
                                game.state.value = 2

                                val movfile = File("$externalFilesDir/${game.game.ReleaseName}/${game.game.ReleaseName}/${game.game.PackageName}")
                                val destfile = File("/storage/emulated/0/Android/obb/${game.game.PackageName}/")

                                if (destfile.exists()) { destfile.deleteRecursively() }

                                moveDirectory(movfile, destfile, {game.MainProgress.value = it})

                                val apkFilePath = "$externalFilesDir/${game.game.ReleaseName}/${game.game.ReleaseName}"  //${game.game.PackageName}.apk"

                                val installTXTpath = "$externalFilesDir/${game.game.ReleaseName}/${game.game.ReleaseName}/install.txt"

                                if(File(installTXTpath).exists()) {
                                    ParseInstallTXT(context, installTXTpath)
                                } else {
                                    for (file in File(apkFilePath).listFiles()!!) {
                                        if (file.name.endsWith(".apk")) {
                                            println("Starting apk install:" + file.path)
                                            installApk(context, file.absolutePath, game.game)
                                        }
                                    }
                                }
                            }
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

                    //CLEANUP
                    if (File(hashfolder).exists()) {
                        File(hashfolder).deleteRecursively()
                    }
                    if (File(obbFolder).exists()) {
                        File(obbFolder).deleteRecursively()
                    }

                    game.IsClosing.value = true

                    progressNzip.cancelChildren()

                    gamelist.removeAt(0)

                    if (!gamelist.isEmpty()) {
                        Startinstall(context, gamelist)
                    }   else {
                        if(canUseShizuku() && IsStopSleep) {
                            ShizAdbCommand("am broadcast -a com.oculus.vrpowermanager.automation_disable")
                        }
                    }
                    cancel()
                }
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.P)
fun installApk(context: Context, apkpath:String, game:Game) {
    println("INSTALLAPK:  $apkpath")
    val file = File(apkpath)
    if(canUseShizuku()) {
        println("SHIZUKU INSTALL AVAILABLE")

        println(
            ShizAdbCommand("cp \"${file.path}\"  \"/data/local/tmp/${file.name}\"; " +
                "pm install \"/data/local/tmp/${file.name}\";" +
                "rm \"/data/local/tmp/${file.name}\";" +
                    "rm \"${file.path}\"")
        )
    } else {
        val apkUri = FileProvider.getUriForFile(context, ".fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {setDataAndType(apkUri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        startActivity(context, intent, null)
        GlobalScope.launch {
            while (true) {
                delay(1000)

                val installedPackages = context.packageManager.getInstalledPackages(PackageManager.GET_ACTIVITIES)

                for (packageInfo in installedPackages) {
                    val packageName = packageInfo.packageName
                    //check when apk is installed
                    if (packageName == game.PackageName) {
                        println("PACKAGENAME EQUAL")
                        val packageInfo = context.packageManager.getPackageInfo(game.PackageName, 0);
                        println(packageInfo.longVersionCode)
                        println(game.VersionCode)
                        if (packageInfo.longVersionCode == game.VersionCode.toLong()) {
                            println("GAME IS MATCHING VERSION. UNINSTALLING")
                            file.delete()
                            cancel()
                        }
                    }
                    //println(packageName)
                }
            }
        }
    }
}

fun ParseInstallTXT(context: Context, txtPath:String) {
    val installtxt = File(txtPath).readText()
    var temptxt = ""
    val localDir =  File(txtPath).path.removeSuffix("install.txt")
    if(canUseShizuku()) {
        for(line in installtxt.lines()) {
            if(line.startsWith("adb shell")) {
                temptxt += "${line.removePrefix("adb shell ")}; \n"
            }
            if(line.startsWith("adb install")) {
                val apkpath = "$localDir${line
                    .removePrefix("adb install")
                    .removePrefix(" -g")
                    .removePrefix(" -r")
                    .removePrefix(" -k")
                    .removePrefix(" ")}"

                val apkname = File(apkpath).name

                temptxt += "cp \"$apkpath\" /data/local/tmp/; " +
                            "pm install -g -r /data/local/tmp/$apkname; " +
                            "rm /data/local/tmp/$apkname; "

                //"pm install -g -r \"$localDir${line.removePrefix("adb install -g -r ")}\"; "
            }
            if(line.startsWith("adb pull")) {
                temptxt += "mv \"${line.removePrefix("adb pull ")}\" \"$localDir\"; "
            }
            if(line.startsWith("adb push")) {
                val tempsplit = splitArguments(line.removePrefix("adb push "))
                temptxt += "mv \"$localDir${tempsplit[0]}\" \"${tempsplit[1]}\"; "
            }
        }

        println("ORIGINAL INSTALL TXT: ")
        println(installtxt)
        println("PARSED INSTALL TXT: ")
        println(temptxt)

        println("CONSOLE RESPONSE: ")
        //println(ShizAdbCommand(temptxt))
        val tempval = temptxt.split(";")
        for(i in tempval) {
            println("command:")
            println(i)
            println("result:")
            println(ShizAdbCommand(i))
        }
    }
}

fun splitArguments(input: String): List<String> {
    val regex = """("[^"]*"|\S+)""".toRegex()
    return regex.findAll(input).map { it.value.trim('"') }.toList()
}

fun moveDirectory(sourceDirectory: File, targetDirectory: File, progressCallback: (Float) -> Unit) {
    val sourceFiles = sourceDirectory.listFiles() ?: return
    val totalFiles = sourceFiles.size

    var processedFiles = 0

    fun moveFilesRecursive(sourceDirectory:File, targetDirectory: File) {
        val sourceFiless = sourceDirectory.listFiles() ?: return
        for (file in sourceFiless) {
            if (file.isDirectory) {
                val newTargetDirectory = File(targetDirectory, file.name)
                newTargetDirectory.mkdir()
                moveFilesRecursive(file, newTargetDirectory)
            } else {
                file.copyTo(File(targetDirectory, file.name))
                processedFiles++
                val progress = (processedFiles.toDouble() / totalFiles.toDouble()).toFloat()
                progressCallback(progress)
            }
        }
    }

    moveFilesRecursive(sourceDirectory, targetDirectory)

}
