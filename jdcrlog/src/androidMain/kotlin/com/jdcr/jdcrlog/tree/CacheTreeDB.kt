package com.jdcr.jdcrlog.tree

import android.util.Log
import com.jdcr.jdcrbase.app.JdcrAppUtils
import com.jdcr.jdcrbase.coroutine.JdcrSafeCoroutineScope
import com.jdcr.jdcrbase.log.JdcrLogData
import com.jdcr.jdcrlog.JdcrLogBase
import com.jdcr.jdcrlog.log.JdcrTimber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.locks.ReentrantLock

internal class CacheTreeDB : JdcrTimber.Tree() {

    private val minLevel = JdcrLogBase.miniLevel
    private var coroutine = JdcrSafeCoroutineScope(Dispatchers.IO, tag = "jdcrLog")
    private val lock = ReentrantLock()
    private val cache = ArrayList<JdcrLogData>(64)

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

    private suspend fun writeLog() {
        val batch = ArrayList<JdcrLogData>()
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
    ): JdcrLogData {

        fun getTag(): Triple<String?, String?, String?>? {
            val list = tag?.split(JdcrLogBase.tagDelimiter) ?: return null
            return Triple(list.firstOrNull(), list.getOrNull(1), list.getOrNull(2))
        }

        val time = System.currentTimeMillis()
        return JdcrLogData(
            logTag = tag ?: "null",
            message = message,
            level = priority.toLong(),
            timestamp = time,
            throwable = t,
            tagSplit = getTag(),
            JdcrAppUtils.versionCode,
            JdcrAppUtils.versionName
        )
    }

    private suspend fun writeDB(logs: ArrayList<JdcrLogData>) {
        JdcrLogBase.dbServer?.write(logs)?.onFailure {
            Log.w(JdcrLogBase.baseLogTag, "缓存日志出现异常", it)
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