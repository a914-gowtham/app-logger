package com.gowtham.applogger.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gowtham.applogger.R

class LogAdapter :
    ListAdapter<LogListObject, LogAdapter.LogViewHolder>(LogDiffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.log_row, parent, false)

        return LogViewHolder(view)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        val logs = currentList[position]
        Log.e("TAG", "onBindViewHolder: "+position )
        holder.txtLog.text = AdapterUtil.getSpannedLogs(logs.isLastIndex, logs.list)
    }

    inner class LogViewHolder(view: View) : RecyclerView.ViewHolder(
        view
    ) {
        val txtLog: TextView = view.findViewById(R.id.txt_log)
    }


    fun submitLogList(list: List<List<String>>) {
        val newList = mutableListOf<LogListObject>()

        for (i in list.indices) {
            newList.add(
                LogListObject(list.get(i), i == list.lastIndex)
            )
        }

        submitList(newList.toList())

    }
}
