package com.jdcr.jdcrlog.tree

import android.util.Log
import com.jdcr.jdcrlog.JdcrLog
import com.jdcr.jdcrlog.log.JdcrTimber

class LevelFilterTree(miniLevel: Int? = null) : JdcrTimber.Tree() {

    private val minLevel = miniLevel ?: Log.INFO

    override fun log(
        priority: Int,
        tag: String?,
        message: String,
        t: Throwable?
    ) {
        if (!JdcrLog.selfTree(tag)) {
            return
        }
        if (priority < minLevel) {
            return
        }
        val newTag = tag ?: "jdcr_log"
        if (priority == Log.ASSERT) {
            Log.wtf(newTag, message)
        } else {
            Log.println(priority, newTag, message)
        }
    }
}