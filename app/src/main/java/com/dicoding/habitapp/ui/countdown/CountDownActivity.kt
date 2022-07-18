package com.dicoding.habitapp.ui.countdown

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.dicoding.habitapp.R
import com.dicoding.habitapp.data.Habit
import com.dicoding.habitapp.notification.NotificationWorker
import com.dicoding.habitapp.utils.HABIT
import com.dicoding.habitapp.utils.HABIT_ID
import com.dicoding.habitapp.utils.NOTIFICATION_CHANNEL_ID

class CountDownActivity : AppCompatActivity() {

    private lateinit var oneTimeWorkRequest: OneTimeWorkRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_count_down)
        supportActionBar?.title = "Count Down"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val habit = intent.getParcelableExtra<Habit>(HABIT) as Habit

        findViewById<TextView>(R.id.tv_count_down_title).text = habit.title

        val viewModel = ViewModelProvider(this).get(CountDownViewModel::class.java)

        //TODO 10 : Set initial time and observe current time. Update button state when countdown is finished
        viewModel.setInitialTime(habit.minutesFocus)
        viewModel.currentTimeString.observe(this) {
            val tvCountDown: TextView = findViewById(R.id.tv_count_down)
            tvCountDown.text = it
        }

        //TODO 13 : Start and cancel One Time Request WorkManager to notify when time is up.
        val data = Data.Builder()
            .putInt(HABIT_ID, habit.id)
            .putString(NOTIFICATION_CHANNEL_ID, habit.title)
            .build()
        oneTimeWorkRequest = OneTimeWorkRequest.Builder(NotificationWorker::class.java)
            .setInputData(data)
            .build()
        val workManager = WorkManager.getInstance(this)
        viewModel.setInitialTime(habit.minutesFocus)
        viewModel.eventCountDownFinish.observe(this) {
            updateButtonState(false)
            workManager.enqueue(oneTimeWorkRequest)
        }

        findViewById<Button>(R.id.btn_start).setOnClickListener {
            viewModel.startTimer()
            updateButtonState(true)
        }

        findViewById<Button>(R.id.btn_stop).setOnClickListener {
            viewModel.resetTimer()
            workManager.cancelWorkById(oneTimeWorkRequest.id)
            updateButtonState(false)
        }
    }

    private fun updateButtonState(isRunning: Boolean) {
        findViewById<Button>(R.id.btn_start).isEnabled = !isRunning
        findViewById<Button>(R.id.btn_stop).isEnabled = isRunning
    }
}