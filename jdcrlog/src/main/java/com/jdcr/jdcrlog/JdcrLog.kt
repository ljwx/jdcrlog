package com.jdcr.jdcrlog

import com.jdcr.jdcrlog.tree.CacheTree
import com.jdcr.jdcrlog.tree.LevelFilterTree
import timber.log.Timber

object JdcrLog {

    private var prefix = "jdcr_"

    init {
        enable(false)
    }

    fun enable(enable: Boolean, prefix: String? = null, filePath: String? = null) {
        synchronized(this) {
            Timber.uprootAll()
            prefix?.let { this.prefix = it }
            if (enable) {
                CacheTree.clearOld(filePath)
                Timber.plant(Timber.DebugTree())
                filePath?.let { Timber.plant(CacheTree(it)) }
            } else {
                Timber.plant(LevelFilterTree())
            }
        }
    }

    fun v(tag: String, message: String?) {
        Timber.tag(prefix + tag).v(message)
    }

    fun i(tag: String, msg: String?, t: Throwable? = null) {
        Timber.tag(prefix + tag).i(t, msg)
    }

    fun d(tag: String, msg: String?, t: Throwable? = null) {
        Timber.tag(prefix + tag).d(t, msg)
    }

    fun w(tag: String, msg: String?, t: Throwable?) {
        Timber.tag(prefix + tag).w(t, msg)
    }

    fun e(tag: String, t: Throwable?, msg: String?) {
        Timber.tag(prefix + tag).e(t, msg)
    }

}