package com.catalyst.app_logger

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.gowtham.applogger.AppLogger
import com.gowtham.applogger.LogViewer

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var btnLog: Button
    private lateinit var btnILog: Button
    private lateinit var btnELog: Button
    private lateinit var btnShowLog: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        btnLog = findViewById(R.id.btnLog)
        btnILog = findViewById(R.id.btnInfoLog)
        btnELog = findViewById(R.id.btnErrorLog)
        btnShowLog = findViewById(R.id.btnShowLogs)



        btnLog.setOnClickListener(this)
        btnILog.setOnClickListener(this)
        btnELog.setOnClickListener(this)
        btnShowLog.setOnClickListener(this)
        handler.postDelayed(runnable,200)
    }

    private val runnable= Runnable {
        AppLogger.writeLog(this@MainActivity, "Navigation screen verbose" +
                "log time ${System.currentTimeMillis()}")
        AppLogger.writeILog(this@MainActivity, "Navigation screen " +
                "log time ${System.currentTimeMillis()}")
        postRun()
    }

    fun postRun(){
        handler.removeCallbacks(runnable)
//        handler.postDelayed(runnable,1000)
    }

    val handler= Handler(Looper.getMainLooper())

    override fun onClick(v: View?) {

        when (v?.id) {
            R.id.btnLog -> {
                AppLogger.writeLog(this, "Navigation screen log 1")
            }

            R.id.btnInfoLog -> {
                AppLogger.writeILog(this, "Navigation screen log 3")

            }

            R.id.btnErrorLog -> {
                AppLogger.writeELog(this, "Navigation screen log 4")

            }

            R.id.btnShowLogs -> {
                val logViewer = LogViewer()
                logViewer.show(supportFragmentManager, LogViewer.TAG)
            }
        }


    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable)
    }

    override fun onResume() {
        super.onResume()
        handler.postDelayed(runnable,100)
    }
}