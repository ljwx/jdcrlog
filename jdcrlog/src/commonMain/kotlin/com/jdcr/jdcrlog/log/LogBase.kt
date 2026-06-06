package com.jdcr.jdcrlog.log

interface LogBase {

    fun enable(debug: Boolean, filePath: String? = null)

    fun v(msg: String?)

    fun vF(feature: String, msg: String?)

    fun vT(tag: String, msg: String?)

    fun d(msg: String?, t: Throwable? = null)

    fun dF(feature: String, msg: String?, t: Throwable? = null)

    fun dT(tag: String, msg: String?, t: Throwable? = null)

    fun i(msg: String?, t: Throwable? = null)

    fun iF(feature: String, msg: String?, t: Throwable? = null)

    fun iT(tag: String, msg: String?, t: Throwable? = null)

    fun w(msg: String?, t: Throwable? = null)

    fun wF(feature: String, msg: String?, t: Throwable? = null)

    fun wT(tag: String, msg: String?, t: Throwable? = null)

    fun e(msg: String?, t: Throwable?)

    fun eF(feature: String, msg: String?, t: Throwable?)

    fun eT(tag: String, msg: String?, t: Throwable?)

}