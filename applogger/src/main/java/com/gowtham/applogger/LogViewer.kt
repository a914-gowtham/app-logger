package com.gowtham.applogger

import android.Manifest
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.switchmaterial.SwitchMaterial
import com.gowtham.applogger.adapter.LogAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class LogViewer(var liveLog: Boolean) : BottomSheetDialogFragment() {

    private lateinit var listLog: RecyclerView
    private lateinit var txtClear: TextView
    private lateinit var txtCopy: TextView
    private lateinit var txtEntries: TextView
    private lateinit var liveLogSwitch: SwitchMaterial
    private lateinit var imageScrollBtm: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyView: View

    private val handler = Handler(Looper.myLooper()!!)
    private val REFRESH_DELAY = 800L

    private lateinit var logAdapter: LogAdapter

    private val pattern = "yyyy.MM.dd-HH:mm:ss"
    private val formatter = SimpleDateFormat(pattern, Locale.ENGLISH)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.log_view_dialog, container, false)

    companion object {
        const val TAG = "ModalBottomSheet"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listLog = view.findViewById(R.id.list_log)
        txtClear = view.findViewById(R.id.txt_clear)
        txtCopy = view.findViewById(R.id.txt_copy)
        liveLogSwitch = view.findViewById(R.id.live_switch)
        imageScrollBtm = view.findViewById(R.id.img_scroll_btm)
        progressBar = view.findViewById(R.id.progressbar)
        emptyView = view.findViewById(R.id.empty_view)
        txtEntries = view.findViewById(R.id.txt_entries)

        initAdapter()
        if (liveLog) {
            handler.postDelayed(entryCountRunnable, 0)
        }
        loadData()
        listLog.addOnScrollListener(scrollLister)
        setLiveLogSwitchListener()
        setUpClickedListeners()
    }

    private fun loadData() {
        if (liveLog) {
            handler.removeCallbacks(autoScrollRunnable)
            handler.postDelayed(autoScrollRunnable, 0)
        } else {
            lifecycleScope.launch {
                val logs = getChunkedList()
                withContext(Dispatchers.Main) {
                    logAdapter.submitLogList(logs)
                    handleEmptyState(logs)
                }
            }
        }
    }

    private fun initAdapter() {
        logAdapter = LogAdapter()
        listLog.apply {
            itemAnimator = null
            layoutManager = LinearLayoutManager(requireContext())
            adapter = logAdapter
        }
    }

    private fun handleEmptyState(data: List<List<String>>) {
        if (progressBar.visibility == View.VISIBLE) {
            progressBar.visibility = View.GONE
        }
        if (data.isEmpty()) {
            emptyView.visibility = View.VISIBLE
        } else {
            emptyView.visibility = View.GONE
        }
    }

    private val scrollLister = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)

            if (liveLog && !recyclerView.canScrollVertically(1)) {
                handler.removeCallbacks(autoScrollRunnable)
                handler.postDelayed(autoScrollRunnable, 200)
            }

        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            if (liveLog && dy < 0) {
                handler.removeCallbacks(autoScrollRunnable)
            }
        }
    }

    private fun setLiveLogSwitchListener() {
        liveLogSwitch.isChecked = liveLog
        liveLogSwitch.setOnCheckedChangeListener { _, enabled ->
            liveLog = enabled
            if (enabled) {
                handler.removeCallbacks(autoScrollRunnable)
                handler.postDelayed(entryCountRunnable, 100)
                handler.postDelayed(autoScrollRunnable, 100)
            } else {
                handler.removeCallbacks(entryCountRunnable)
                handler.removeCallbacks(autoScrollRunnable)
            }
        }
    }

    private fun setUpClickedListeners() {

        txtClear.setOnClickListener {
            lifecycleScope.launch {
                AppLogger.clearLogs(requireContext())
                loadData()
            }
        }
        txtCopy.apply {
            setOnClickListener {

                if (Util.hasPublicWritePermission(context)) {
                    storeLogFile()
                } else {
                    requestPermissionLauncher.launch(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                }
            }
        }

        imageScrollBtm.setOnClickListener {
            scrollToEnd()
        }
    }

    fun openSheet(fragmentManager: FragmentManager, tag: String){
        show(fragmentManager, tag)
    }

    private fun scrollToEnd() {
        if (liveLog) {
            postCall()
        }
        listLog.scrollToPosition(logAdapter.currentList.lastIndex)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                storeLogFile()
            } else {
                Log.e(TAG, "storage permission missing!")
            }
        }

    private fun storeLogFile() {
        lifecycleScope.launch(Dispatchers.IO) {
            val logs = AppLogger.readLogs(requireContext())
            val directory = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            )
            val fileName = "app-log-${formatter.format(Date())}.txt"

            val contentValues = ContentValues()
            contentValues.put(MediaStore.Files.FileColumns.DISPLAY_NAME, fileName)
            contentValues.put(MediaStore.Files.FileColumns.MIME_TYPE, "application/text")
            contentValues.put(MediaStore.Files.FileColumns.TITLE, fileName)
            contentValues.put(
                MediaStore.Files.FileColumns.DATE_ADDED,
                System.currentTimeMillis() / 1000
            )
            contentValues.put(
                MediaStore.Files.FileColumns.RELATIVE_PATH,
                Environment.DIRECTORY_DOWNLOADS
            )
            contentValues.put(MediaStore.Files.FileColumns.DATE_TAKEN, System.currentTimeMillis())
            val externalUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                val storedFile = File(directory, fileName)
                if (storedFile.exists()) {
                    storedFile.delete()
                }
                storedFile.createNewFile()
                storedFile.writeText(logs)
                showFileStoreAnimation()
                return@launch
            }

            try {
                val fileUri: Uri? =
                    requireContext().contentResolver.insert(externalUri!!, contentValues)
                val outputStream: OutputStream? =
                    fileUri?.let { requireContext().contentResolver.openOutputStream(it) }
                outputStream?.write(logs.toByteArray())
                outputStream?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            showFileStoreAnimation()

        }
    }

    private suspend fun showFileStoreAnimation() {
        withContext(Dispatchers.Main) {
            txtCopy.setTextAnimation("Saved to downloads..") {
                txtCopy.setTextAnimation("Save file") {

                }
            }
        }
    }

    override fun onDestroyView() {
        handler.removeCallbacks(autoScrollRunnable)
        handler.removeCallbacks(entryCountRunnable)
        super.onDestroyView()
    }

    private val autoScrollRunnable = Runnable {
        lifecycleScope.launch {
            val logs = getChunkedList()
            withContext(Dispatchers.Main) {
                logAdapter.submitLogList(logs)
                handleEmptyState(logs)
                delay(50)
                scrollToEnd()
            }
        }
    }

    private val entryCountRunnable = Runnable {
        lifecycleScope.launch {
            val logs = withContext(Dispatchers.IO) {
                AppLogger.readLogs(requireContext())
            }
            val list = logs.split("\n").filter { it.isNotEmpty() }
            txtEntries.text = "${list.size} entries"
            postEntryCount()
        }
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(entryCountRunnable)
        handler.removeCallbacks(autoScrollRunnable)
    }

    override fun onResume() {
        super.onResume()
        if (liveLog) {
            handler.postDelayed(entryCountRunnable, REFRESH_DELAY)
            handler.postDelayed(autoScrollRunnable, REFRESH_DELAY)
        }
    }

    private fun postEntryCount() {
        handler.removeCallbacks(entryCountRunnable)
        handler.postDelayed(entryCountRunnable, REFRESH_DELAY)
    }

    private fun postCall() {
        handler.removeCallbacks(autoScrollRunnable)
        handler.postDelayed(autoScrollRunnable, REFRESH_DELAY)
    }


    private suspend fun getChunkedList(): List<List<String>> {
        val logs = withContext(Dispatchers.IO) {
            AppLogger.readLogs(requireContext())
        }
        val list = logs.split("\n").filter { it.isNotEmpty() }
        txtEntries.text = "${list.size} entries"
        return list.chunked(15)
    }

}

fun TextView.setTextAnimation(
    text: String,
    duration: Long = 400,
    completion: (() -> Unit)? = null
) {
    fadOutAnimation(duration) {
        this.text = text
        fadInAnimation(duration) {
            completion?.let {
                it()
            }
        }
    }
}

// ViewExtensions

fun View.fadOutAnimation(
    duration: Long = 300,
    visibility: Int = View.INVISIBLE,
    completion: (() -> Unit)? = null
) {
    animate()
        .alpha(0f)
        .setDuration(duration)
        .withEndAction {
            this.visibility = visibility
            completion?.let {
                it()
            }
        }
}

fun View.fadInAnimation(duration: Long = 300, completion: (() -> Unit)? = null) {
    alpha = 0f
    visibility = View.VISIBLE
    animate()
        .alpha(1f)
        .setDuration(duration)
        .withEndAction {
            completion?.let {
                it()
            }
        }
}
