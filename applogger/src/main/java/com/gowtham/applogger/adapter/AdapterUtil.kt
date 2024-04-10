package com.gowtham.applogger.adapter

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import com.gowtham.applogger.AppLogger
import com.gowtham.applogger.LogLevel

object AdapterUtil {

    fun getSpannedLogs(isLastItem: Boolean, logs: List<String>): SpannableStringBuilder {
        val spannableStringBuilder = SpannableStringBuilder()

        for (i in logs.indices) {
            val item= logs.get(i)
            if (item.isNotBlank()) {
                val span = SpannableString(item)
                span.setSpan(
                    ForegroundColorSpan(getSpanColor(item)),
                    0,
                    item.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                spannableStringBuilder.append(
                    span
                )
                if(i!=logs.lastIndex) {
                    spannableStringBuilder.append("\n")
                }
            }
        }
        if (isLastItem) {
            spannableStringBuilder.append("\n-----end-----")
        }
        return spannableStringBuilder
    }

    private fun getSpanColor(logString: String): Int {
        val patternLength = AppLogger.pattern.length
        if (logString.isNotEmpty()) {
            return when (logString.subSequence(patternLength + 1, patternLength + 2)) {
                LogLevel.DEBUG.value -> {
                    Color.GREEN
                }

                LogLevel.INFO.value -> {
                    Color.parseColor("#ACCBE1")
                }

                LogLevel.ERROR.value -> {
                    Color.parseColor("#F95738")
                }

                LogLevel.WARN.value -> {
                    Color.parseColor("#FFBF00")
                }

                else -> {
                    Color.GRAY
                }
            }
        }
        return Color.GRAY
    }

}