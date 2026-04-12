package com.jdcr.jdcrlog

import android.util.Log
import com.jdcr.jdcrlog.tree.CacheTree
import com.jdcr.jdcrlog.tree.LevelFilterTree
import timber.log.Timber

open class JdcrLogBase(private val prefix: String = "jdcr_", feature: String = "log") {

    private val featureTag = prefix + feature

    init {
        enable(false)
    }

    fun enable(enable: Boolean, filePath: String? = null) {
        synchronized(this) {
            Log.d("jdcr_log", "是否开启日志:$enable,日志文件缓存路径:$filePath")
            Timber.uprootAll()
            if (enable) {
                CacheTree.clearOld(filePath)
                Timber.plant(Timber.DebugTree())
                filePath?.let { Timber.plant(CacheTree(it)) }
            } else {
                Timber.plant(LevelFilterTree())
            }
        }
    }

    fun v(message: String?) {
        v(featureTag, message)
    }

    fun vF(feature: String, message: String?) {
        v(prefix + feature, message)
    }

    fun v(tag: String, message: String?) {
        Timber.tag(tag).v(message)
    }

    fun i(msg: String?, t: Throwable? = null) {
        i(featureTag, msg, t)
    }

    fun iF(feature: String, msg: String?, t: Throwable? = null) {
        i(prefix + feature, msg, t)
    }

    fun i(tag: String, msg: String?, t: Throwable? = null) {
        Timber.tag(tag).i(t, msg)
    }

    fun d(msg: String?, t: Throwable? = null) {
        d(featureTag, msg, t)
    }

    fun dF(feature: String, msg: String?, t: Throwable? = null) {
        d(prefix + feature, msg, t)
    }

    fun d(tag: String, msg: String?, t: Throwable? = null) {
        Timber.tag(tag).d(t, msg)
    }

    fun w(msg: String?, t: Throwable?) {
        w(featureTag, msg, t)
    }

    fun wF(feature: String, msg: String?, t: Throwable? = null) {
        w(prefix + feature, msg, t)
    }

    fun w(tag: String, msg: String?, t: Throwable?) {
        Timber.tag(tag).w(t, msg)
    }

    fun e(t: Throwable?, msg: String?) {
        e(featureTag, t, msg)
    }

    fun eF(feature: String, t: Throwable? = null, msg: String?) {
        e(prefix + feature, t, msg)
    }

    fun e(tag: String, t: Throwable?, msg: String?) {
        Timber.tag(tag).e(t, msg)
    }

}

object JdcrLog : JdcrLogBase() {


}