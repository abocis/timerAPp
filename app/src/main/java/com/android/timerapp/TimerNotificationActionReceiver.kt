package com.android.timerapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.android.timerapp.util.NotificationUtil
import com.android.timerapp.util.prefUtil

class TimerNotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {


        when (intent.action){
            AppConstants.ACTION_STOP -> {
                MainActivity.removeAlarm(context)
                prefUtil.setTimerState(MainActivity.TimerState.Stopped, context)
                NotificationUtil.hideTimerNotification(context)
            }
            AppConstants.ACTION_PAUSE -> {
                var secondsRemaining = prefUtil.getsSecondsRemaining(context)
                val alarmSetTime = prefUtil.getAlarmSetTime(context)
                val nowSeconds = MainActivity.nowSeconds

                secondsRemaining -= nowSeconds - alarmSetTime
                prefUtil.setSecondsRemaining(secondsRemaining,context)

                MainActivity.removeAlarm(context)
                prefUtil.setTimerState(MainActivity.TimerState.Paused, context)
                NotificationUtil.showTimerPause(context)

            }
            AppConstants.ACTION_RESUME -> {

                val secondsRemaing = prefUtil.getsSecondsRemaining(context)
                val wakeUpTime = MainActivity.setAlarm(context, MainActivity.nowSeconds, secondsRemaing )
                prefUtil.setTimerState(MainActivity.TimerState.Running, context)
                NotificationUtil.showTimerRunning(context, wakeUpTime)

            }
            AppConstants.ACTION_START -> {
                val minutesRemaining = prefUtil.getTimerLength(context)
                val secondsRemaining = minutesRemaining * 60L
                val wakeUpTime = MainActivity.setAlarm(context, MainActivity.nowSeconds, secondsRemaining )
                prefUtil.setTimerState(MainActivity.TimerState.Running, context)
                prefUtil.setSecondsRemaining(secondsRemaining,context)
                NotificationUtil.showTimerRunning(context, wakeUpTime)
            }

        }

    }
}