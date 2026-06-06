package com.jdcr.jdcrlog.tree

import android.util.Log
import com.jdcr.jdcrlog.JdcrLogBase
import com.jdcr.jdcrlog.util.keepLastNLines
import com.jdcr.jdcrlog.log.JdcrTimber
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock

internal class CacheTree : JdcrTimber.Tree() {

    companion object {

        fun clearOld() {
            val file = File(JdcrLogBase.filePath)
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

    private val minLevel = JdcrLogBase.miniLevel
    private val cache = ArrayList<String>(32)
    private val lock = ReentrantLock()
    private val released = AtomicBoolean(false)
    private val executor = Executors.newSingleThreadScheduledExecutor()
    private val flushTask: ScheduledFuture<*> = executor.scheduleWithFixedDelay(
        {
            if (!released.get()) {
                writeLog()
            }
        },
        1250,
        1250,
        TimeUnit.MILLISECONDS
    )

    private val sdfThreadLocal = object : ThreadLocal<SimpleDateFormat>() {
        override fun initialValue(): SimpleDateFormat {
            return SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.getDefault())
        }
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
        if (released.get() || priority < minLevel) {
            return
        }
        lock.lock()
        try {
            if (released.get()) {
                return
            }
            cache.add(getMessage(priority, tag, message, t))
        } finally {
            lock.unlock()
        }
    }

    private fun writeLog() {
        val batch = ArrayList<String>()
        lock.lock()
        try {
            batch.addAll(cache)
            cache.clear()
        } finally {
            lock.unlock()
        }
        if (batch.isEmpty()) {
            return
        }
        val result = buildString { batch.forEach { append(it) } }
        writeFile(result)
    }

    private fun getMessage(
        priority: Int,
        tag: String?,
        message: String,
        t: Throwable?
    ): String {
        val time = sdfThreadLocal.get().format(Date())
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
            val file = File(JdcrLogBase.filePath)
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

    fun release() {
        if (!released.compareAndSet(false, true)) {
            return
        }
        // Wait for any in-flight log() calls that already passed the fast-path check.
        lock.lock()
        try {
        } finally {
            lock.unlock()
        }
        flushTask.cancel(false)
        executor.execute { writeLog() }
        executor.shutdown()
    }

}