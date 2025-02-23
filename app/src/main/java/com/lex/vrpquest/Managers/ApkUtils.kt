package com.lex.vrpquest.Managers
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.*
import android.content.pm.PackageInstaller
import android.os.Bundle
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.OutputStream

class InstallResultReceiver : BroadcastReceiver() {
    @SuppressLint("UnsafeIntentLaunch")
    override fun onReceive(context: Context, intent: Intent) {
        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -1)
        val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)

        println("STATUS HERE :" + status)
        when (status) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                // User must confirm via system dialog
                val confirmationIntent = intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
                confirmationIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(confirmationIntent)
            }
            PackageInstaller.STATUS_SUCCESS -> Log.d("Install", "Success!")
            PackageInstaller.STATUS_FAILURE -> Log.e("Install", "Failed: $message")
            PackageInstaller.STATUS_FAILURE_ABORTED -> Log.e("Install", "Aborted!")
        }
    }
}

fun installApk(context: Context, apkFile: File) {
    val packageInstaller = context.packageManager.packageInstaller
    val params = PackageInstaller.SessionParams(
        PackageInstaller.SessionParams.MODE_FULL_INSTALL
    ).apply {
        setSize(apkFile.length()) // Optional but recommended
    }

    try {
        val sessionId = packageInstaller.createSession(params)
        val session = packageInstaller.openSession(sessionId)

        // Write APK to session
        FileInputStream(apkFile).use { inputStream ->
            session.openWrite("apk", 0, apkFile.length()).use { outputStream ->
                inputStream.copyTo(outputStream)
                session.fsync(outputStream) // Ensure data is flushed
            }
        }

        // Create PendingIntent for installation result
        val intent = Intent(context, InstallResultReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            sessionId,
            intent,
            PendingIntent.FLAG_MUTABLE
        )

        // Commit the session
        session.commit(pendingIntent.intentSender)
        session.close()
    } catch (e: IOException) {
        e.printStackTrace()
    } catch (e: SecurityException) {
        e.printStackTrace()
    }
}


