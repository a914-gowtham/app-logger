package com.gowtham.applogger

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

object AppLogger {


    internal const val pattern = "MMM.dd HH:mm:ss.SSS"
    private val formatter = SimpleDateFormat(pattern, Locale.ENGLISH)

    private val loggerScope = CoroutineScope(Dispatchers.IO)

    private var maxSize: Int = 10

    private val _logFlow = MutableStateFlow("")
    val logFlow: StateFlow<String> = _logFlow
    private var logFile: File? = null

    private var logPreference: LogPreference?= null


    @JvmStatic
    fun setMaxFileSize(sizeInMB: Int) {
        maxSize = sizeInMB
    }

    @JvmStatic
    fun writeLog(context: Context, msg: String) {
        if(logPreference==null){
            logPreference= LogPreference(context)
        }
        if (!logPreference!!.isLogEnabled()){
            return
        }
        checkMaxFile(context)
        if (msg.isNotEmpty()) {
            dispatcher.dispatch(Dispatchers.IO) {
                _logFlow.value = msg
                getLogFile(context).appendText(formatter.format(Date()) + ": $msg" + "\n")
            }
        }
    }

    private fun getLogFile(context: Context): File {
        if (logFile == null) {
            logFile = File(context.filesDir, "app-log.txt")
        }
        return logFile!!
    }

    private val dispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()


    @JvmStatic
    fun writeWLog(context: Context, msg: String) {
        if(logPreference==null){
            logPreference= LogPreference(context)
        }
        if (!logPreference!!.isLogEnabled()){
            return
        }
        checkMaxFile(context)
        if (msg.isNotEmpty()) {
            dispatcher.dispatch(Dispatchers.IO) {
                _logFlow.value = msg
                getLogFile(context).appendText(formatter.format(Date()) + ":${LogLevel.WARN.value} $msg" + "\n")
            }
        }
    }

    @JvmStatic
    fun writeDLog(context: Context, msg: String) {
        if(logPreference==null){
            logPreference= LogPreference(context)
        }
        if (!logPreference!!.isLogEnabled()){
            return
        }
        checkMaxFile(context)
        if (msg.isNotEmpty()) {
            dispatcher.dispatch(Dispatchers.IO) {
                _logFlow.value = msg
                getLogFile(context).appendText(formatter.format(Date()) + ":${LogLevel.DEBUG.value} $msg" + "\n")
            }
        }
    }

    @JvmStatic
    fun writeELog(context: Context, msg: String) {
        if(logPreference==null){
            logPreference= LogPreference(context)
        }
        if (!logPreference!!.isLogEnabled()){
            return
        }
        checkMaxFile(context)
        if (msg.isNotEmpty()) {
            dispatcher.dispatch(Dispatchers.IO) {
                _logFlow.value = msg
                getLogFile(context).appendText(formatter.format(Date()) + ":${LogLevel.ERROR.value} $msg" + "\n")
            }
        }
    }

    @JvmStatic
    fun writeILog(context: Context, msg: String) {
        if(logPreference==null){
            logPreference= LogPreference(context)
        }
        if (!logPreference!!.isLogEnabled()){
            return
        }
        checkMaxFile(context)
        if (msg.isNotEmpty()) {
            dispatcher.dispatch(Dispatchers.IO) {
                getLogFile(context).appendText(formatter.format(Date()) + ": $msg" + "\n")
            }
        }
    }

    @JvmStatic
    fun writeVLog(context: Context, msg: String) {
        if(logPreference==null){
            logPreference= LogPreference(context)
        }
        if (!logPreference!!.isLogEnabled()){
            return
        }
        checkMaxFile(context)
        if (msg.isNotEmpty()) {
            dispatcher.dispatch(Dispatchers.IO) {
                _logFlow.value = msg
                getLogFile(context).appendText(formatter.format(Date()) + ":${LogLevel.VERBOSE.value} $msg" + "\n")
            }
        }
    }


    @JvmStatic
    suspend fun readLogs(context: Context): String {
        checkMaxFile(context)
        val file = getLogFile(context)
        if (!file.exists()) {
            withContext(Dispatchers.IO) {
                file.createNewFile()
            }
        }
        return loggerScope.async { file.readText() }.await()
    }

    @JvmStatic
    suspend fun clearLogs(context: Context) {
        val file = getLogFile(context)
        _logFlow.value = "clear"
        loggerScope.async { file.writeText("") }.await()
    }

    private fun checkMaxFile(context: Context) {
        loggerScope.launch {
            val file = getLogFile(context)

            if (file.exists()) {
                val fileSizeInBytes = file.length()
                val fileSizeInKB = fileSizeInBytes / 1024
                val fileSizeInMB = fileSizeInKB / 1024
                if (fileSizeInMB > maxSize) {
                    clearLogs(context)
                }
            }
        }
    }
}