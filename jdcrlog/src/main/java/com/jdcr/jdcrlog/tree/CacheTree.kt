package com.jdcr.jdcrlog.tree

import android.util.Log
import com.jdcr.jdcrlog.util.keepLastNLines
import timber.log.Timber
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CacheTree(private val filePath: String) : Timber.Tree() {

    companion object {
        private const val dateFormat = "MM-dd HH:mm:ss.SSS"

        private val sdf = SimpleDateFormat(dateFormat, Locale.getDefault())

        fun clearOld(filePath: String?) {
            if (filePath.isNullOrEmpty()) {
                return
            }
            val file = File(filePath)
            if (!file.exists()) {
                return
            }
            val maxSize = 1024 * 1024 * 2
            if (file.length() > maxSize) {
                runCatching {
                    file.keepLastNLines(700)
                    Log.w("jdcr_log", "日志缓存清理完成")
                }
            }
        }

    }

    private val minLevel = Log.DEBUG

    private fun initFile(file: File) {
        val parent = file.parentFile
        if (parent != null && !parent.exists()) {
            parent.mkdirs()
        }
        if (!file.exists()) {
            file.createNewFile()
        }
    }

    override fun log(
        priority: Int,
        tag: String?,
        message: String,
        t: Throwable?
    ) {
        if (priority < minLevel) {
            return
        }
        var writer: FileWriter?
        try {
            val file = File(filePath)
            initFile(file)
            writer = FileWriter(file, true)
            writer.use {
                val time = sdf.format(Date())
                writer.write("$time $tag: $message\n")
                if (t != null) {
                    writer.write("${Log.getStackTraceString(t)}\n")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}