package com.jdcr.jdcrlog.tree

import android.util.Log
import com.jdcr.jdcrbase.JdcrAppUtils
import com.jdcr.jdcrbase.JdcrSafeCoroutineScope
import com.jdcr.jdcrlog.JdcrLogBase
import com.jdcr.jdcrlog.log.JdcrTimber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.locks.ReentrantLock

data class LogData(
    val tag: String?,
    val message: String,
    val level: Int,
    val timestamp: Long,
    val throwable: Throwable?,
    val tagSplit: Triple<String?, String?, String?>?,
    val versionCode: Long,
    val versionName: String
)

internal class CacheTreeDB : JdcrTimber.Tree() {

    private val minLevel = JdcrLogBase.miniLevel
    private var coroutine = JdcrSafeCoroutineScope(Dispatchers.IO, tag = "jdcrLog")
    private val lock = ReentrantLock()
    private val cache = ArrayList<LogData>(64)

    init {
        coroutine.launch {
            while (isActive) {
                writeLog()
                delay(1500)
            }
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
        lock.lock()
        try {
            cache.add(getMessage(priority, tag, message, t))
        } finally {
            lock.unlock()
        }
    }

    private fun writeLog() {
        val batch = ArrayList<LogData>()
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
        writeDB(batch)
    }

    private fun getMessage(
        priority: Int,
        tag: String?,
        message: String,
        t: Throwable?
    ): LogData {

        fun getTag(): Triple<String?, String?, String?>? {
            val list = tag?.split(JdcrLogBase.tagDelimiter) ?: return null
            return Triple(list.firstOrNull(), list.getOrNull(1), list.getOrNull(2))
        }

        val time = System.currentTimeMillis()
        return LogData(
            tag = tag, message = message, level = priority, time, t, getTag(),
            JdcrAppUtils.versionCode, JdcrAppUtils.versionName
        )
    }

    private fun writeDB(logs: ArrayList<LogData>) {
        try {
            JdcrLogBase.dbServer?.invoke(logs)
        } catch (e: Exception) {
            Log.w(JdcrLogBase.baseLogTag, "缓存日志出现异常", e)
        }
    }

    fun release() {
        coroutine.coroutineContext.cancelChildren()
        // Wait for any in-flight log() calls that already passed the fast-path check.
        lock.lock()
        try {
        } finally {
            lock.unlock()
        }
    }

}