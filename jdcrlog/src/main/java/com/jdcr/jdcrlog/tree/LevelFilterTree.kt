package com.jdcr.jdcrlog.tree

import android.util.Log
import timber.log.Timber

class LevelFilterTree : Timber.Tree() {

    private val minLevel = Log.INFO

    override fun log(
        priority: Int,
        tag: String?,
        message: String,
        t: Throwable?
    ) {
        if (priority >= minLevel) {
            super.log(priority, tag, message, t)
        }
    }
}