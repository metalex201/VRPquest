package com.lex.vrpquest

import android.content.pm.PackageManager
import android.os.ParcelFileDescriptor
import moe.shizuku.server.IShizukuService
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Reader

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

fun canUseShizuku():Boolean {
    if (Shizuku.pingBinder()) {
        return (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED)
    } else { return false }
}

fun ShizAdbCommand(command:String):String {
    println("ADB COMMAND: $command")
    val process = IShizukuService.Stub.asInterface(Shizuku.getBinder()).newProcess(arrayOf("sh","-c",command), null, null)
    val mInput = BufferedReader(ParcelFileDescriptor.AutoCloseInputStream(process.inputStream).reader())
    process.waitFor()
    println("readtext: ${mInput.readText()}")
    return mInput.readText()
}




