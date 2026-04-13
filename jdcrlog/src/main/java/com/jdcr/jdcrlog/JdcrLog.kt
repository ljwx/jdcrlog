package com.jdcr.jdcrlog

import android.util.Log
import com.jdcr.jdcrlog.tree.CacheTree
import com.jdcr.jdcrlog.tree.LevelFilterTree
import timber.log.Timber

object JdcrLog : LogBase {

    var globalLogPrefix = "jdcr_"
    var globalLogFeature = "log"

    private val prefix = globalLogPrefix
    private val feature = globalLogFeature
    private val featureTag = prefix + feature

    private inline fun <reified T> hasPlanted(): Boolean {
        return Timber.forest().any { it is T }
    }

    override fun enable(debug: Boolean, filePath: String?) {
        synchronized(this) {
            Log.d("jdcr_log", "初始化,是否开启debug日志:$debug,日志文件缓存路径:$filePath")
            if (debug) {
                CacheTree.clearOld(filePath)
                if (!hasPlanted<Timber.DebugTree>()) {
                    Timber.plant(Timber.DebugTree())
                }
                if (!hasPlanted<CacheTree>()) {
                    filePath?.let { Timber.plant(CacheTree(it)) }
                }
            } else {
                if (!hasPlanted<LevelFilterTree>()) {
                    Timber.plant(LevelFilterTree())
                }
            }
        }
    }

    internal fun selfTree(tag: String?): Boolean {
        return tag?.startsWith(globalLogPrefix) == true
    }

    override fun v(message: String?) {
        vT(featureTag, message)
    }

    override fun vF(feature: String, message: String?) {
        vT(prefix + feature, message)
    }

    override fun vT(tag: String, message: String?) {
        Timber.tag(tag).v(message)
    }

    override fun i(msg: String?, t: Throwable?) {
        iT(featureTag, msg, t)
    }

    override fun iF(feature: String, msg: String?, t: Throwable?) {
        iT(prefix + feature, msg, t)
    }

    override fun iT(tag: String, msg: String?, t: Throwable?) {
        Timber.tag(tag).i(t, msg)
    }

    override fun d(msg: String?, t: Throwable?) {
        dT(featureTag, msg, t)
    }

    override fun dF(feature: String, msg: String?, t: Throwable?) {
        dT(prefix + feature, msg, t)
    }

    override fun dT(tag: String, msg: String?, t: Throwable?) {
        Timber.tag(tag).d(t, msg)
    }

    override fun w(msg: String?, t: Throwable?) {
        wT(featureTag, msg, t)
    }

    override fun wF(feature: String, msg: String?, t: Throwable?) {
        wT(prefix + feature, msg, t)
    }

    override fun wT(tag: String, msg: String?, t: Throwable?) {
        Timber.tag(tag).w(t, msg)
    }

    override fun e(msg: String?, t: Throwable?) {
        eT(featureTag, msg, t)
    }

    override fun eF(feature: String, msg: String?, t: Throwable?) {
        eT(prefix + feature, msg, t)
    }

    override fun eT(tag: String, msg: String?, t: Throwable?) {
        Timber.tag(tag).e(t, msg)
    }

}