package com.lex.vrpquest

import android.content.pm.PackageManager
import moe.shizuku.server.IShizukuService
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

fun onRequestPermissionsResult(requestCode: Int, grantResult: Int) {
    val granted = grantResult == PackageManager.PERMISSION_GRANTED
    // Do stuff based on the result and the request code
}

val REQUEST_PERMISSION_RESULT_LISTENER =
    Shizuku.OnRequestPermissionResultListener { requestCode: Int, grantResult: Int ->
        onRequestPermissionsResult(
            requestCode,
            grantResult
        )
    }
fun checkPermission(code: Int): Boolean {
    if (Shizuku.isPreV11()) {
        // Pre-v11 is unsupported
        return false
    }

    if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
        // Granted
        return true
    } else if (Shizuku.shouldShowRequestPermissionRationale()) {
        // Users choose "Deny and don't ask again"
        return false
    } else {
        // Request the permission
        Shizuku.requestPermission(code)
        return false
    }
}

fun IsShizukuGranted():Boolean {
    return (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED)

}

fun ShizAdbCommand(command:String):String {
    val command = ""
    val process = IShizukuService.Stub.asInterface(Shizuku.getBinder()).newProcess(arrayOf("sh","-c",command), null, null)

    val inputStream = process.inputStream.use { it }
    val stringBuilder = StringBuilder()

    // 读取执行结果
    val inputStreamReader = process.inputStream
    inputStreamReader.close()
    process.waitFor()
    return stringBuilder.toString()

}
private data class ShellResult(val resultCode: Int, val out: String)

