package com.jdcr.jdcrlog

import android.util.Log
import com.jdcr.jdcrbase.JdcrAppUtils
import com.jdcr.jdcrlog.tree.CacheTree
import com.jdcr.jdcrlog.tree.LevelFilterTree
import com.jdcr.jdcrlog.log.JdcrTimber
import com.jdcr.jdcrlog.log.LogBase
import com.jdcr.jdcrlog.tree.CacheTreeDB
import com.jdcr.jdcrlog.tree.LogData

actual open class JdcrLogBase : LogBase {

    companion object {
        val defaultPrefix = "jdcr"
        internal val tagDelimiter = "_"
        var globalLogPrefix: String? = null
            set(value) {
                field = value
                prefix = "${value ?: defaultPrefix}$tagDelimiter"
            }

        @Volatile
        var dbServer: ((ArrayList<LogData>) -> Unit)? = null
        internal val baseLogTag = "${defaultPrefix}${tagDelimiter}log_base"
        internal val filePath by lazy { JdcrAppUtils.getAppContext().cacheDir.absolutePath + "/debug/log/log.txt" }
        private var prefix = "${globalLogPrefix ?: defaultPrefix}$tagDelimiter"

        @Volatile
        internal var miniLevel: Int = Log.INFO
    }

    private var feature = "log"
    private var partition: String? = null
    private var defaultTag = prefix + feature

    fun updateDefaultTag() {
        defaultTag =
            prefix + feature + (if (partition.isNullOrEmpty()) "" else "$tagDelimiter$partition")
    }

    actual fun setDefaultTag(feature: String, partition: String?) {
        this.feature = feature
        this.partition = partition
        updateDefaultTag()
    }

    private inline fun <reified T> hasPlanted(): Boolean {
        return JdcrTimber.forest().any { it is T }
    }

    private inline fun <reified T> removeTree() {
        JdcrTimber.forest().forEach {
            if (it is T) {
                if (it is CacheTree) {
                    it.release()
                }
                if (it is CacheTreeDB) {
                    it.release()
                }
                JdcrTimber.uproot(it)
                Log.d(baseLogTag, "移除:${it.javaClass.name}")
            }
        }
    }

    private inline fun <reified T : JdcrTimber.Tree> plantTree(debug: Boolean, tree: T?) {
        if (tree == null) return
        if (!hasPlanted<T>()) {
            Log.d(baseLogTag, "添加日志树:${T::class.java.simpleName}")
            JdcrTimber.plant(tree)
        }
    }

    fun setConfig(debug: Boolean) {
        synchronized(this) {
            Log.d(baseLogTag, "初始化,是否开启debug日志:$debug")
            updateDefaultTag()
            miniLevel = if (debug) Log.DEBUG else Log.INFO
            if (debug) {
                removeTree<LevelFilterTree>()
                plantTree(true, JdcrTimber.DebugTree())
                if (dbServer == null) {
                    CacheTree.clearOld()
                    plantTree(true, CacheTree())
                } else {
                    plantTree(true, CacheTreeDB())
                }
            } else {
                removeTree<CacheTree>()
                removeTree<JdcrTimber.DebugTree>()
                plantTree(false, LevelFilterTree())
            }
            Log.d(baseLogTag, "初始化完成,默认tag:$defaultTag")
        }
    }

    override fun enable(debug: Boolean, filePath: String?) {
        setConfig(debug)
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