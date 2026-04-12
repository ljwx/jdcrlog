package com.jdcr.jdcrlog.tree

import android.util.Log
import timber.log.Timber

class LevelFilterTree(miniLevel: Int? = null) : Timber.Tree() {

    private val minLevel = miniLevel ?: Log.INFO

    override fun log(
        priority: Int,
        tag: String?,
        message: String,
        t: Throwable?
    ) {
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