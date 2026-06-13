package com.jdcr.jdcrlog

import com.jdcr.jdcrlog.log.LogBase

actual open class JdcrLogBase actual constructor() : LogBase {
    actual fun setDefaultTag(feature: String, partition: String?) {}
    override fun enable(debugMode: Boolean) {}
    override fun v(msg: String?) {}
    override fun vF(feature: String, msg: String?) {}
    override fun vT(tag: String, msg: String?) {}
    override fun d(msg: String?, t: Throwable?) {}
    override fun dF(feature: String, msg: String?, t: Throwable?) {}
    override fun dT(tag: String, msg: String?, t: Throwable?) {}
    override fun i(msg: String?, t: Throwable?) {}
    override fun iF(feature: String, msg: String?, t: Throwable?) {}
    override fun iT(tag: String, msg: String?, t: Throwable?) {}
    override fun w(msg: String?, t: Throwable?) {}
    override fun wF(feature: String, msg: String?, t: Throwable?) {}
    override fun wT(tag: String, msg: String?, t: Throwable?) {}
    override fun e(msg: String?, t: Throwable?) {}
    override fun eF(feature: String, msg: String?, t: Throwable?) {}
    override fun eT(tag: String, msg: String?, t: Throwable?) {}
}

actual object JdcrLog : JdcrLogBase()
