package com.android.timerapp.util

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.android.timerapp.AppConstants
import com.android.timerapp.MainActivity
import com.android.timerapp.R
import com.android.timerapp.TimerNotificationActionReceiver
import java.text.SimpleDateFormat
import java.util.Date

class NotificationUtil {

    companion object {
        private const val CHANEL_ID_TIMER = "menu_timer"
        private const val  CHANEL_NAME_TIMER = "TIMER APP TIMER"
        private const val TIMER_ID = 0

        fun showTimerExpired(context: Context){
            val startIntent = Intent(context,TimerNotificationActionReceiver::class.java)
            startIntent.action = AppConstants.ACTION_START
            val startPendingIntent = PendingIntent.getBroadcast(context,
                0,startIntent, PendingIntent.FLAG_UPDATE_CURRENT)

            val nBuilder = getBasisNotificationBuilder(context, CHANEL_ID_TIMER, true  )
            nBuilder.setContentTitle("Timer Expired")
                .setContentText("Start again?")
                .setContentIntent(getPendingIntentWithStack(context, MainActivity::class.java))
                .addAction(R.drawable.ic_play_circle, "start", startPendingIntent)

            val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nManager.createNotificationChannel(CHANEL_ID_TIMER, CHANEL_NAME_TIMER,true)

            nManager.notify(TIMER_ID, nBuilder.build())
        }

        fun showTimerRunning(context: Context, WakeUpTime: Long){
            val stopIntent = Intent(context,TimerNotificationActionReceiver::class.java)
            stopIntent.action = AppConstants.ACTION_STOP
            val stopPendingIntent = PendingIntent.getBroadcast(context,
                0,stopIntent, PendingIntent.FLAG_UPDATE_CURRENT)

            val pauseIntent = Intent(context,TimerNotificationActionReceiver::class.java)
                pauseIntent.action = AppConstants.ACTION_PAUSE
            val pausePendingIntent = PendingIntent.getBroadcast(context,
                0,pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT)

            val df = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT)

            val nBuilder = getBasisNotificationBuilder(context, CHANEL_ID_TIMER, true  )
            nBuilder.setContentTitle("Timer is Running")
                .setContentText("End: ${df.format(Date(WakeUpTime))}")
                .setContentIntent(getPendingIntentWithStack(context, MainActivity::class.java))
                .setOngoing(true)
                .addAction(R.drawable.ic_stop24, "Stop", stopPendingIntent)
                .addAction(R.drawable.ic_pause_circle, "pause", pausePendingIntent)

            val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nManager.createNotificationChannel(CHANEL_ID_TIMER, CHANEL_NAME_TIMER,true)

            nManager.notify(TIMER_ID, nBuilder.build())

        }
        fun showTimerPause(context: Context){
            val resumeIntent = Intent(context,TimerNotificationActionReceiver::class.java)
            resumeIntent.action = AppConstants.ACTION_RESUME
            val resumePendingIntent = PendingIntent.getBroadcast(context,
                0,resumeIntent, PendingIntent.FLAG_UPDATE_CURRENT)

            val nBuilder = getBasisNotificationBuilder(context, CHANEL_ID_TIMER, true  )
            nBuilder.setContentTitle("Timer Pause")
                .setContentText("Resume?")
                .setContentIntent(getPendingIntentWithStack(context, MainActivity::class.java))
                .setOngoing(true)
                .addAction(R.drawable.ic_play_circle, "Resume", resumePendingIntent)

            val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nManager.createNotificationChannel(CHANEL_ID_TIMER, CHANEL_NAME_TIMER,true)

            nManager.notify(TIMER_ID, nBuilder.build())

        }
        fun hideTimerNotification(context: Context){
            val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nManager.cancel(TIMER_ID)

        }




        private fun getBasisNotificationBuilder(context: Context, chanelId: String, playSound: Boolean)
        : NotificationCompat.Builder{
            val notificationSound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val nBuilder = NotificationCompat.Builder(context, chanelId)
                .setSmallIcon(R.drawable.baseline_timer_24)
                .setAutoCancel(true)
                .setDefaults(0)

            if (playSound) nBuilder.setSound(notificationSound)

            return nBuilder
        }
        private fun <T> getPendingIntentWithStack (context: Context, javaClass: Class <T>): PendingIntent{
            val resultIntent = Intent(context, javaClass)
            resultIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

            val stackBuilder = TaskStackBuilder.create(context)
            stackBuilder.addParentStack(javaClass)
            stackBuilder.addNextIntent(resultIntent)
            return stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT)
        }


        @TargetApi(26)
        private fun NotificationManager.createNotificationChannel(chanelID: String, chanelName: String, playSound: Boolean){

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                val chanelImportance = if (playSound) NotificationManager.IMPORTANCE_DEFAULT
                else NotificationManager.IMPORTANCE_LOW
                val nchanel = NotificationChannel(chanelID, chanelName, chanelImportance)
                nchanel.enableLights(true)
                nchanel.lightColor = Color.BLUE
                this.createNotificationChannel(nchanel)
            }
        }
    }
}