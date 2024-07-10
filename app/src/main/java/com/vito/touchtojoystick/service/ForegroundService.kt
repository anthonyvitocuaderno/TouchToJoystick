package com.vito.touchtojoystick.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.vito.touchtojoystick.R

class ForegroundService : Service() {

    companion object {
        var instance: ForegroundService? = null
    }
    private var window: Window? = null

    fun getWindow(): Window? {
        return window
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.d("TEST", "onBind")
        return null
    }

    override fun onCreate() {
        Log.d("TEST", "onCreate")
        super.onCreate()
        // create the custom or default notification
        // based on the android version
        startMyOwnForeground()

        // create an instance of Window class
        // and display the content on screen
        window = Window(this)
        window!!.open()

        ForegroundService.instance = this
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d("TEST", "onStartCommand")
        window?.open()

        ForegroundService.instance = this
        return START_STICKY
    }

    // for android version >=O we need to create
    // custom notification stating
    // foreground service is running
    private fun startMyOwnForeground() {
        Log.d("TEST", "STARTING FOREGROUND SERVICE...")

        val NOTIFICATION_CHANNEL_ID = "vito.overlay"
        val channelName = "Background Service"
        val chan = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            channelName,
            NotificationManager.IMPORTANCE_MIN,
        )
        val manager = (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
        manager.createNotificationChannel(chan)
        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        val notification: Notification = notificationBuilder.setOngoing(true)
            .setContentTitle("Service running")
            .setContentText("Displaying over other apps") // this is important, otherwise the notification will show the way
            // you want i.e. it will show some default notification
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(2, notification)
    }
}
