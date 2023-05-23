package com.android.timerapp

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.util.concurrent.TimeUnit

class TimerForegroundService : Service() {
    private lateinit var timer: CountDownTimer
    private var timerLengthSeconds: Long = 0
    private var secondsRemaining: Long = 0

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val action = intent.action
            when (action) {
                ACTION_START_TIMER -> {
                    val timerLengthSeconds = intent.getLongExtra(EXTRA_TIMER_LENGTH_SECONDS, 0)
                    startTimer(timerLengthSeconds)
                }
                ACTION_STOP_TIMER -> {
                    stopTimer()
                }
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTimer()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun startTimer(timerLengthSeconds: Long) {
        this@TimerForegroundService.timerLengthSeconds = timerLengthSeconds
        secondsRemaining = timerLengthSeconds

        timer = object : CountDownTimer(timerLengthSeconds * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                secondsRemaining = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished)
                updateNotification()
            }

            override fun onFinish() {
                stopSelf()
            }
        }
        timer.start()
    }

    private fun stopTimer() {
        if (::timer.isInitialized) {
            timer.cancel()
        }
        stopSelf()
    }

    private fun createNotification(): Notification {
        val channelId = "timer_channel"
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Timer",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }

        val stopIntent = Intent(this, TimerForegroundService::class.java)
        stopIntent.action = ACTION_STOP_TIMER
        val stopPendingIntent =
            PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Timer")
            .setContentText(formatTime(secondsRemaining))
            .setSmallIcon(R.drawable.baseline_timer_24)
            .setOngoing(true)
            .addAction(R.drawable.ic_stop24, "Stop", stopPendingIntent)
            .build()
    }

    private fun updateNotification() {
        val notification = createNotification()
        val notificationManager = NotificationManagerCompat.from(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun formatTime(seconds: Long): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val ACTION_START_TIMER = "com.android.timerapp.ACTION_START_TIMER"
        private const val ACTION_STOP_TIMER = "com.android.timerapp.ACTION_STOP_TIMER"
        private const val EXTRA_TIMER_LENGTH_SECONDS =
            "com.android.timerapp.EXTRA_TIMER_LENGTH_SECONDS"

        fun startTimer(context: Context, timerLengthSeconds: Long) {
            val intent = Intent(context, TimerForegroundService::class.java)
            intent.action = ACTION_START_TIMER
            intent.putExtra(EXTRA_TIMER_LENGTH_SECONDS, timerLengthSeconds)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopTimer(context: Context) {
            val intent = Intent(context, TimerForegroundService::class.java)
            intent.action = ACTION_STOP_TIMER
            context.stopService(intent)
        }
    }
}