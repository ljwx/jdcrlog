package com.jdcr.jdcrlog

import android.util.Log
import com.jdcr.jdcrlog.tree.CacheTree
import com.jdcr.jdcrlog.tree.LevelFilterTree
import com.jdcr.jdcrlog.log.JdcrTimber
import com.jdcr.jdcrlog.log.LogBase

actual open class JdcrLogBase : LogBase {

    companion object {
        var globalLogPrefix: String? = null
        internal val baseLogTag = "jdcr_log_base"
        internal var filePath: String? = null
    }

    private val prefix by lazy { (globalLogPrefix ?: "jdcr") + "_" }
    private var feature = "log"
    private var partition: String? = null
    private val defaultTag by lazy {
        prefix + feature + (if (partition.isNullOrEmpty()) "" else "_$partition")
    }

    actual fun setDefaultTag(feature: String, partition: String?) {
        this.feature = feature
        this.partition = partition
    }

    private inline fun <reified T> hasPlanted(): Boolean {
        return JdcrTimber.forest().any { it is T }
    }

    private inline fun <reified T> clearTree() {
        JdcrTimber.forest().forEach {
            if (it is T) {
                if (it is CacheTree) {
                    it.release()
                }
                JdcrTimber.uproot(it)
                Log.d(baseLogTag, "移除:${it.javaClass.name}")
            }
        }
    }

    override fun enable(debug: Boolean, filePath: String?) {
        synchronized(this) {
            Log.d(baseLogTag, "初始化,是否开启debug日志:$debug,日志文件缓存路径:$filePath")
            if (debug) {
                CacheTree.clearOld(filePath)
                clearTree<LevelFilterTree>()
                if (!hasPlanted<JdcrTimber.DebugTree>()) {
                    Log.d(baseLogTag, "添加debug树")
                    JdcrTimber.plant(JdcrTimber.DebugTree())
                }
                if (!hasPlanted<CacheTree>()) {
                    Log.d(baseLogTag, "添加cache树")
                    filePath?.let { JdcrTimber.plant(CacheTree(it, Log.DEBUG)) }
                    Companion.filePath = filePath
                }
            } else {
                clearTree<CacheTree>()
                clearTree<JdcrTimber.DebugTree>()
                if (!hasPlanted<LevelFilterTree>()) {
                    Log.d(baseLogTag, "添加level树")
                    JdcrTimber.plant(LevelFilterTree(Log.INFO))
                }
            }
            Log.d(baseLogTag, "初始化完成,默认tag:$defaultTag")
        }
    }

    override fun v(message: String?) {
        vT(defaultTag, message)
    }

    override fun vF(feature: String, message: String?) {
        vT(prefix + feature, message)
    }

    override fun vT(tag: String, message: String?) {
        JdcrTimber.tag(tag).v(message)
    }

    override fun i(msg: String?, t: Throwable?) {
        iT(defaultTag, msg, t)
    }

    override fun iF(feature: String, msg: String?, t: Throwable?) {
        iT(prefix + feature, msg, t)
    }

    override fun iT(tag: String, msg: String?, t: Throwable?) {
        JdcrTimber.tag(tag).i(t, msg)
    }

    override fun d(msg: String?, t: Throwable?) {
        dT(defaultTag, msg, t)
    }

    override fun dF(feature: String, msg: String?, t: Throwable?) {
        dT(prefix + feature, msg, t)
    }

    override fun dT(tag: String, msg: String?, t: Throwable?) {
        JdcrTimber.tag(tag).d(t, msg)
    }

    override fun w(msg: String?, t: Throwable?) {
        wT(defaultTag, msg, t)
    }

    override fun wF(feature: String, msg: String?, t: Throwable?) {
        wT(prefix + feature, msg, t)
    }

    override fun wT(tag: String, msg: String?, t: Throwable?) {
        JdcrTimber.tag(tag).w(t, msg)
    }

    override fun e(msg: String?, t: Throwable?) {
        eT(defaultTag, msg, t)
    }

    override fun eF(feature: String, msg: String?, t: Throwable?) {
        eT(prefix + feature, msg, t)
    }

    override fun eT(tag: String, msg: String?, t: Throwable?) {
        JdcrTimber.tag(tag).e(t, msg)
    }

}

actual object JdcrLog : JdcrLogBase() {

    init {
        setDefaultTag("log")
    }

}