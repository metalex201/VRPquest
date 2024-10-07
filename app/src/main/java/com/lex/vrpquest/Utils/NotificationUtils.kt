package com.lex.vrpquest.Utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.lex.vrpquest.R


fun showNotification(context:Context, message: String) {
    val channelId = "VRP"
    val channelName = "VRP"
    val notificationId = 1
    var builder = NotificationCompat.Builder(context, "VRP")
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle("VRPquest")
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)

    val notificationChannel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(notificationChannel)


    notificationManager.notify(notificationId, builder.build())
}