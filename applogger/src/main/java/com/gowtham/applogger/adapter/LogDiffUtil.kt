package com.gowtham.applogger.adapter

import androidx.recyclerview.widget.DiffUtil

object LogDiffUtil : DiffUtil.ItemCallback<LogListObject>() {
    override fun areItemsTheSame(oldItem: LogListObject, newItem: LogListObject): Boolean {
        return oldItem.list.joinToString(",") == newItem.list.joinToString(",")
    }

    override fun areContentsTheSame(oldItem: LogListObject, newItem: LogListObject): Boolean {
        return oldItem.list.joinToString(",") == newItem.list.joinToString(",") && oldItem.isLastIndex == newItem.isLastIndex
    }
}