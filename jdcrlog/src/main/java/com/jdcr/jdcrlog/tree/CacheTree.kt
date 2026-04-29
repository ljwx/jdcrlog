package com.jdcr.jdcrlog.tree

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.jdcr.jdcrlog.JdcrLogBase
import com.jdcr.jdcrlog.util.keepLastNLines
import com.jdcr.jdcrlog.log.JdcrTimber
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CacheTree(private val filePath: String, miniLevel: Int? = null) : JdcrTimber.Tree() {

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
            val maxSize = 1024 * 1024 * 1.8
            if (file.length() > maxSize) {
                runCatching {
                    file.keepLastNLines(850)
                    Log.w(JdcrLogBase.baseLogTag, "日志缓存清理完成")
                }
            }
        }

    }

    private val minLevel = miniLevel ?: Log.DEBUG
    private val handler = Handler(Looper.getMainLooper())
    private val cache = ArrayList<String>(32)
    private val task = object : Runnable {
        override fun run() {
            if (cache.isNotEmpty()) {
                writeLog()
            }
            handler.postDelayed(this, 1250)
        }
    }

    init {
        handler.postDelayed(task, 1250)
    }

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
        cache.add(getMessage(priority, tag, message, t))
    }

    private fun writeLog() {
        val iterator = cache.iterator()
        val result = buildString {
            while (iterator.hasNext()) {
                val item = iterator.next()
                append(item)
                iterator.remove()
            }
        }
        writeFile(result)
    }

    private fun getMessage(
        priority: Int,
        tag: String?,
        message: String,
        t: Throwable?
    ): String {
        val time = sdf.format(Date())
        val level = when (priority) {
            Log.VERBOSE -> "V"
            Log.DEBUG -> "D"
            Log.INFO -> "I"
            Log.WARN -> "W"
            Log.ERROR -> "E"
            Log.ASSERT -> "A"
            else -> "U"
        }
        return if (t == null) {
            "$time $level $tag: $message\n"
        } else {
            "$time $level $tag: $message\n${Log.getStackTraceString(t)}\n"
        }
    }

    private fun writeFile(message: String) {
        var writer: FileWriter?
        try {
            val file = File(filePath)
            initFile(file)
            writer = FileWriter(file, true)
            writer.use {
                writer.write(message)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.w(JdcrLogBase.baseLogTag, "缓存日志出现异常", e)
        }
    }

}