package com.android.timerapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.android.timerapp.util.NotificationUtil
import com.android.timerapp.util.prefUtil

class TimerExpiredReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
       //show notification
        NotificationUtil.showTimerExpired(context)
        prefUtil.setTimerState(MainActivity.TimerState.Stopped, context)
        prefUtil.setAlarmSetTime(0,context)
    }
}