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
        vT(featureTag, message)
    }

    fun vF(feature: String, message: String?) {
        vT(prefix + feature, message)
    }

    fun vT(tag: String, message: String?) {
        Timber.tag(tag).v(message)
    }

    fun i(msg: String?, t: Throwable? = null) {
        iT(featureTag, msg, t)
    }

    fun iF(feature: String, msg: String?, t: Throwable? = null) {
        iT(prefix + feature, msg, t)
    }

    fun iT(tag: String, msg: String?, t: Throwable? = null) {
        Timber.tag(tag).i(t, msg)
    }

    fun d(msg: String?, t: Throwable? = null) {
        dT(featureTag, msg, t)
    }

    fun dF(feature: String, msg: String?, t: Throwable? = null) {
        dT(prefix + feature, msg, t)
    }

    fun dT(tag: String, msg: String?, t: Throwable? = null) {
        Timber.tag(tag).d(t, msg)
    }

    fun w(msg: String?, t: Throwable?) {
        wT(featureTag, msg, t)
    }

    fun wF(feature: String, msg: String?, t: Throwable? = null) {
        wT(prefix + feature, msg, t)
    }

    fun wT(tag: String, msg: String?, t: Throwable?) {
        Timber.tag(tag).w(t, msg)
    }

    fun e(t: Throwable?, msg: String?) {
        eT(featureTag, t, msg)
    }

    fun eF(feature: String, t: Throwable? = null, msg: String?) {
        eT(prefix + feature, t, msg)
    }

    fun eT(tag: String, t: Throwable?, msg: String?) {
        Timber.tag(tag).e(t, msg)
    }

}

object JdcrLog : JdcrLogBase() {


}