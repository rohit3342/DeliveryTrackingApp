package com.korbit.deliverytrackingapp.core.logging

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central logging layer. All app components should log via this interface
 * so that we can add crash reporting, sampling, or redaction in one place.
 */
interface AppLogger {
    fun v(tag: String, message: String, throwable: Throwable? = null)
    fun d(tag: String, message: String, throwable: Throwable? = null)
    fun i(tag: String, message: String, throwable: Throwable? = null)
    fun w(tag: String, message: String, throwable: Throwable? = null)
    fun e(tag: String, message: String, throwable: Throwable? = null)
}

@Singleton
class AppLoggerImpl @Inject constructor() : AppLogger {

    override fun v(tag: String, message: String, throwable: Throwable?) {
        if (throwable != null) Log.v(tag, message, throwable) else Log.v(tag, message)
    }

    override fun d(tag: String, message: String, throwable: Throwable?) {
        if (throwable != null) Log.d(tag, message, throwable) else Log.d(tag, message)
    }

    override fun i(tag: String, message: String, throwable: Throwable?) {
        if (throwable != null) Log.i(tag, message, throwable) else Log.i(tag, message)
    }

    override fun w(tag: String, message: String, throwable: Throwable?) {
        if (throwable != null) Log.w(tag, message, throwable) else Log.w(tag, message)
    }

    override fun e(tag: String, message: String, throwable: Throwable?) {
        if (throwable != null) Log.e(tag, message, throwable) else Log.e(tag, message)
    }
}
