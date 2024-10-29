package com.lex.vrpquest.Managers

import android.content.Context
import android.content.pm.ApplicationInfo.FLAG_SYSTEM
import android.content.pm.PackageInfo
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.lex.vrpquest.Pages.RemoveFromCheckList
import com.lex.vrpquest.Utils.SettingGetStringSet
import com.lex.vrpquest.Utils.ZipUtil
import com.lex.vrpquest.Utils.decryptPassword
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.StringReader
import java.net.URL
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.Properties
import java.util.Random
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


class DonateGame(
    val packageinfo : PackageInfo,
    var checked: MutableState<Boolean> = mutableStateOf(false),
)
class DonateQueue(
    var packageName: MutableState<String> = mutableStateOf(""),
    var MainProgress: MutableState<Float> = mutableStateOf(0.0f),
    var progressList: SnapshotStateList<Float> = mutableStateListOf<Float>(),
    var speed: MutableState<Int> = mutableStateOf(0),
    var state: MutableState<Int> = mutableStateOf(0),
    var IsActive: MutableState<Boolean> = mutableStateOf(false),
    var IsClosing: MutableState<Boolean> = mutableStateOf(false)
)
@RequiresApi(Build.VERSION_CODES.P)
fun getDonateGames(context: Context, gamelist: MutableList<Game>): MutableList<DonateGame> {

    val packageManager = context.packageManager

    val packages = packageManager.getInstalledPackages(0)

    val userInstalledPackages = packages.filter { packageInfo ->
        (packageInfo.applicationInfo.flags and FLAG_SYSTEM) == 0
    }

    //BLACKLIST
    val blacklistFile = File("/storage/emulated/0/Android/data/com.lex.vrpquest/files/meta/.meta/nouns/blacklist.txt")
    val blacklist = blacklistFile.readLines()

    //PACKAGENAME LIST
    val packageNameList: List<String> = gamelist.map { game -> game.PackageName }
    println(packageNameList)

    //LOOKUP TABLE FOR PACKAGENAME TO VERSION, TO CHECK IF VERSION IS NEWER THAN PREVIOUS GAME
    val versionLookup: MutableMap<String, String> = mutableMapOf()

    gamelist.forEach() {
        versionLookup.put(it.PackageName, it.VersionCode)
    }

    println(versionLookup)

    //SAVED BLACKLIST
    val UserBlacklist = (SettingGetStringSet(context, "DonateBlacklist") ?: setOf())
    var returnList:MutableList<DonateGame> = mutableStateListOf()

    for (packageInfo in userInstalledPackages) {
        val packageName = packageInfo.packageName
        //println(packageName)
        if (!blacklist.contains(packageName) && !UserBlacklist.contains(packageName)) {
            if(packageNameList.contains(packageName)) {
                if (packageInfo.longVersionCode > versionLookup[packageName]!!.toInt()) {
                    println("VERSION IS NEWER:" + packageName)
                    println("package ver:" + packageInfo.longVersionCode)
                    println("server package ver :" + versionLookup[packageName]!!.toInt())
                    returnList.add(DonateGame(packageinfo = packageInfo))
                } //else { returnList.add(DonateGame(packageinfo = packageInfo)) } //THIS IS ILLEGAL, ONLY USED FOR TESTING REMOVE LATER!!!!!!
            } else {
                println("not in blacklist : " + packageName)
                returnList.add(DonateGame(packageinfo = packageInfo))
            }
        }

    }

    return returnList
}

fun StartDonation(context: Context, donatelist:MutableList<DonateQueue>) {
    println("startdonation" + donatelist)
    println("startdonation" + donatelist.size)
    val game = donatelist.firstOrNull()
    if(game != null) {
        val dispatch = CoroutineScope(Dispatchers.IO)
        dispatch.launch(Dispatchers.IO) {
            println("GAME OMG GAME :" + game)
            if (!game.IsActive.value) {
                game.IsActive.value = true
                var IsFinished = false
                val externalFilesDir = context.getExternalFilesDir(null)?.absolutePath.toString()
                val temp = File("$externalFilesDir/donateTemp")

                dispatch.launch(Dispatchers.IO) {
                    val applicationInfo = context.packageManager.getApplicationInfo(game.packageName.value, 0);
                    val packageInfo = context.packageManager.getPackageInfo(game.packageName.value, 0);
                    val apkpath = applicationInfo.publicSourceDir
                    val appName = applicationInfo.loadLabel(context.getPackageManager()).toString()
                    val random = Random().nextInt(9).toString()
                    val codename= android.os.Build.MODEL ?: ""

                    val fullName = "$appName ${packageInfo.versionName} ${game.packageName.value} $random $codename";

                    println("FULLNAME : \n" + fullName)

                    //ZIP processs

                    if (temp.exists()) {
                        temp.deleteRecursively()
                    }
                    temp.mkdirs()

                    val zipdir = "$externalFilesDir/donateTemp/$fullName.zip"

                    val localOBB = File("/storage/emulated/0/Android/obb/${game.packageName.value}/")
                    val apkZipPath = File("$externalFilesDir/donateTemp/${game.packageName.value}.apk")

                    val ziplist = if(localOBB.exists()) {listOf(File(apkpath), localOBB)} else {listOf(File(apkpath))}

                    game.state.value = 0
                    ZipUtil().zip(ziplist, File(zipdir), Pair(File(apkpath), apkZipPath), {game.MainProgress.value = it})
                    game.state.value = 1

                    disableSSLCertificateChecking()
                    val config = URL("https://vrpirates.wiki/downloads/vrp.upload.config").readText()
                    enableSSLCertificateChecking()

                    val properties = Properties()
                    properties.load(StringReader(config))

                    val host = properties.getProperty("host")
                    val user = properties.getProperty("user")
                    val pass = decryptPassword(properties.getProperty("pass"))!! //"VrpDonati0ns" //properties.getProperty("pass")
                    val port = properties.getProperty("port").toInt()

                    println("CONFIG VALUES : $host, $user $pass, $port")


                    val sizetxt = "$externalFilesDir/donateTemp/$fullName.txt"
                    File(sizetxt).writeText(File("$externalFilesDir/donateTemp/$fullName.zip").length().toString())


                    val client = FTPconnect(user, pass, "$host:$port") ?: return@launch
                    FTPuploadFile(client, sizetxt, "/", {game.MainProgress.value = it})
                    FTPuploadFile(client, zipdir, "/", {game.MainProgress.value = it})

                    IsFinished = true
                }

                dispatch.launch(Dispatchers.IO) {
                    while (!game.IsClosing.value) {
                        if (IsFinished) {
                            if (temp.exists()) {
                                temp.deleteRecursively()
                            }

                            RemoveFromCheckList(context, game.packageName.value)

                            game.IsClosing.value = true
                        }
                        delay(1)
                    }
                    if (!IsFinished) {
                        if (temp.exists()) {
                            temp.deleteRecursively()
                        }
                    }
                    if (!donatelist.isEmpty()) {donatelist.removeAt(0)}

                    if (!donatelist.isEmpty()) {
                        StartDonation(context, donatelist)
                    }
                }
            }
        }
    }
}

fun RemoveDonatedGame(index:Int, gamelist:MutableList<DonateQueue>) {
    println("index is : $index")
    if (index == 0) { gamelist[0].IsClosing.value = true } else {
        gamelist.removeAt(index)
    }
}

//networking
fun disableSSLCertificateChecking() {
    val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
        override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
        override fun checkClientTrusted(arg0: Array<X509Certificate>, arg1: String) {}
        override fun checkServerTrusted(arg0: Array<X509Certificate>, arg1: String) {}
    })
    val sc = SSLContext.getInstance("TLS")
    sc.init(null, trustAllCerts, SecureRandom())
    HttpsURLConnection.setDefaultSSLSocketFactory(sc.socketFactory)
}
fun enableSSLCertificateChecking() {
    val defaultSSLSocketFactory = SSLContext.getDefault().getSocketFactory()
    HttpsURLConnection.setDefaultSSLSocketFactory(defaultSSLSocketFactory)
}


fun decrypt(dataToDecrypt: ByteArray, cipherText: ByteArray): ByteArray {
    val c = Cipher.getInstance("AES/CBC/NoPadding")
    val key = SecretKeySpec(cipherText, "AES")
    c.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(ByteArray(16)))
    val ccipherText = c.doFinal(dataToDecrypt)
    return ccipherText
}