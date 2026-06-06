package com.jdcr.jdcrlog

import com.jdcr.jdcrlog.log.LogBase

expect open class JdcrLogBase() : LogBase {
    fun setDefaultTag(feature: String = "log", partition: String? = null)
}

expect object JdcrLog : JdcrLogBase