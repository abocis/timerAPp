package com.android.timerapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Menu
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.android.timerapp.util.NotificationUtil
import com.android.timerapp.util.prefUtil
import com.google.android.material.floatingactionbutton.FloatingActionButton
import me.zhanghai.android.materialprogressbar.MaterialProgressBar
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    companion object{
        fun setAlarm(context: Context, nowSeconds: Long, secondsRemaining: Long): Long{
            val wakUpTime = (nowSeconds + secondsRemaining) * 1000
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, TimerExpiredReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context,0,intent,
                PendingIntent.FLAG_IMMUTABLE)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, wakUpTime, pendingIntent)
            prefUtil.setAlarmSetTime(nowSeconds, context)

            return wakUpTime
        }

        fun removeAlarm(context: Context){
            val intent = Intent (context, TimerExpiredReceiver ::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
            prefUtil.setAlarmSetTime(0, context)
        }

        val  nowSeconds: Long
        get() = Calendar.getInstance().timeInMillis / 1000


    }

    enum class TimerState{
        Stopped, Paused, Running
    }

    private lateinit var timer: CountDownTimer
    private var timerLengthSeconds: Long = 0

    private var timerState = TimerState.Stopped
    private var secondsRemaining = 0L
    lateinit var  playButton: FloatingActionButton
    lateinit var pauseButton:FloatingActionButton
    lateinit var stopButton: FloatingActionButton
    private lateinit var  progress_Countdown: MaterialProgressBar
    private lateinit var textView_Countdown: TextView


    private fun initializeViews(){

        playButton = findViewById(R.id.playButton)
        pauseButton = findViewById(R.id.pauseButton)
        stopButton = findViewById(R.id.stopButton)
        progress_Countdown = findViewById(R.id.progress_Countdown)
        textView_Countdown = findViewById(R.id.textView_Countdown)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar= findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setIcon(R.drawable.baseline_timer_24)
        supportActionBar?.title = "   Självövervakning"


        initializeViews()

      playButton.setOnClickListener {v ->
          startTimer()
          timerState = TimerState.Running
          updateButton()
      }

      pauseButton.setOnClickListener {v ->
          timer.cancel()
          timerState = TimerState.Paused
          updateButton()
      }

      stopButton.setOnClickListener {v ->
          timer.cancel()
          onTimerFinished()
      }

    }

    override fun onResume() {
        super.onResume()

        initTimer()

        /** remove background timer,*/
        removeAlarm(this)
        // hide notification
        NotificationUtil.hideTimerNotification(this)


    }

    override fun onPause() {
        super.onPause()
        if (timerState == TimerState.Running){
            timer.cancel()
            /**start background timer */
            val wakeUpTime = setAlarm(this, nowSeconds, secondsRemaining)
        // show notification
            NotificationUtil.showTimerRunning(this, wakeUpTime)

        }else if (timerState == TimerState.Paused){
            // show the notification
            NotificationUtil.showTimerPause(this)
        }

        prefUtil.setPreviousTimerLengthSeconds(timerLengthSeconds, this)
        prefUtil.setSecondsRemaining(secondsRemaining, this)
        prefUtil.setTimerState(timerState,this)
    }
    private fun initTimer() {
        timerState = prefUtil.getTimerState(this)

        setNewTimerLenght()

        if( timerState == TimerState.Stopped) {
            setNewTimerLenght()
        }else{
            setPreviousTimerLenght()
        }

        secondsRemaining = if (timerState == TimerState.Running || timerState == TimerState.Paused) {
            prefUtil.getsSecondsRemaining(this)
        }else{
            timerLengthSeconds

        }
        // Change the secondRemaining according to where the background timer stopp
        val alarmSetTime = prefUtil.getAlarmSetTime(context = this)
        if(alarmSetTime > 0){
            secondsRemaining -= nowSeconds -alarmSetTime
        }

        if (secondsRemaining <= 0 ){
            onTimerFinished()

        }else if (timerState == TimerState.Running)

            startTimer()

        updateButton()
        updateCountDownUI()
    }

    private fun onTimerFinished() {
       timerState = TimerState.Stopped
        TimerForegroundService.stopTimer(this)

        //set the length of the timer to be the one set in SettingsActivity
        //if the length was changed when the timer was running

        setNewTimerLenght()

        progress_Countdown.progress = 0

        prefUtil.setSecondsRemaining(timerLengthSeconds,this)
        secondsRemaining = timerLengthSeconds

        updateButton()
        updateCountDownUI()

    }


    private fun startTimer() {
        timerState = TimerState.Running
        TimerForegroundService.startTimer(this,secondsRemaining)
        updateButton()

        timer = object : CountDownTimer(secondsRemaining * 1000,1000 ) {

            override fun onFinish() = onTimerFinished()

            override fun onTick(millisUntilFinished: Long) {
                secondsRemaining = millisUntilFinished / 1000

                updateCountDownUI()
            }
        }.start()
    }

    private fun setNewTimerLenght() {
        val lengthInMinute = prefUtil.getTimerLength(this)
        timerLengthSeconds = (lengthInMinute * 60L)
        progress_Countdown.max = timerLengthSeconds.toInt()

    }
    private fun setPreviousTimerLenght(){
        timerLengthSeconds = prefUtil.getPreviousTimerLengthSeconds(this)
        progress_Countdown.max = timerLengthSeconds.toInt()
    }
    private fun  updateCountDownUI(){
        val minutsUntilFinished = secondsRemaining / 60L
        val secondsInMinuteUntilFinished = secondsRemaining - minutsUntilFinished * 60
        val secondsStr = secondsInMinuteUntilFinished.toString()
        textView_Countdown.text = "$minutsUntilFinished : ${
            if (secondsStr.length == 2 ) secondsStr
            else "0" + secondsStr }"
        progress_Countdown.progress = (timerLengthSeconds - secondsRemaining).toInt()

    }
    private fun updateButton() {
        when(timerState){
            TimerState.Running -> {
                playButton.isEnabled = false
                pauseButton.isEnabled = true
                stopButton.isEnabled = true
            }
            TimerState.Stopped -> {
                playButton.isEnabled = true
                pauseButton.isEnabled = false
                stopButton.isEnabled = false

            }
            TimerState.Paused-> {
                playButton.isEnabled = true
                pauseButton.isEnabled = false
                stopButton.isEnabled = true
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // inflate the menu; this adds items to the action bar if it is present
        menuInflater.inflate(R.menu.menu_timer, menu)

        return true
    }
}