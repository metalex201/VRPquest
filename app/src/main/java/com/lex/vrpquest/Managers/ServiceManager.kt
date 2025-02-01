package com.lex.vrpquest.Managers

import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.compose.material3.Text
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import com.lex.vrpquest.R
import java.lang.Thread.sleep


fun startservice(context: Context) {
    val isservice = isServiceRunning(context, "com.lex.vrpquest.Managers.DownloadService")
    if(!isservice) {
        val intent = Intent(context, DownloadService::class.java)
        context.startService(intent)
    }
}


fun isServiceRunning(context: Context, servicename: String): Boolean {
    val manager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
    for (service in manager.getRunningServices(Int.MAX_VALUE)) {
        println(service.service.className)
        if (servicename == service.service.className) {
            return true
        }
    }
    return false
}



class DownloadService : Service() {
    companion object {
        var _queueList = MutableLiveData<MutableList<QueueGame>>(mutableListOf())
    }


    private val CHANNEL_ID = "download_service_channel"

    override fun onCreate() {
        super.onCreate()

        // Create notification channel for devices running Android O and above
        val name = "Download Service"
        val descriptionText = "Channel for download service"

        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)


        // Start the foreground service with a notification
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Download in progress")
            .setContentText("Downloading files...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        startForeground(1, notification)  // Use 1 as notification ID
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Thread {
            updateNotification("", "",1)
            while (true) {
                val temp = _queueList.value?.firstOrNull()

                var temptext = ""


                if (temp != null) {
                    when(temp!!.state.value) {
                        0 -> { temptext = "Downloading" }
                        1 -> { temptext = "Extracting" }
                        2 -> { temptext = "Moving obb" }
                        3 -> { temptext = "Downloading obb" }
                        4 -> { temptext = "Downloading apk" }
                    }

                    sleep(100)
                    updateNotification("Processing ${temp.game.GameName}",
                        temptext,
                        (temp.MainProgress.value * 100).toInt())
                }
            }
        }.start()

        Startinstall(this, _queueList.value ?: mutableListOf()) {
            println("notify end")
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(1)

        }
        //stopSelf()
        return START_STICKY
    }

    private fun updateNotification(title:String, desc:String, progress: Int) {
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText("$desc $progress%")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setProgress(100, progress, false)
            .build()

        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notification)  // Update the notification

    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}