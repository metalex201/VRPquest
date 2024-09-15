package com.lex.vrpquest

import android.content.pm.PackageManager
import android.os.ParcelFileDescriptor
import android.os.RemoteException
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuRemoteProcess
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream

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

fun IsShizukuConnected():Boolean {
    return (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED)
}

