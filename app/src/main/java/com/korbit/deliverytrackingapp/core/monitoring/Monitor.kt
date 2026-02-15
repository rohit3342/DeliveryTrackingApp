package com.korbit.deliverytrackingapp.core.monitoring

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Generic observability/monitoring interface for functional components.
 * Use for events, metrics, and breadcrumbs to debug and monitor the app.
 * Current implementation logs with Log.i; can be extended to send to
 * analytics, crash reporting, or APM without changing call sites.
 */
interface Monitor {

    /**
     * Record a named event from a component (e.g. "sync_started", "task_action_sent").
     * @param component Logical component (e.g. "sync_engine", "task_detail").
     * @param eventName Event name for filtering and dashboards.
     * @param attributes Optional key-value pairs; values should be serializable.
     */
    fun recordEvent(component: String, eventName: String, attributes: Map<String, Any?> = emptyMap())

    /**
     * Record a numeric metric (e.g. count, duration_ms).
     * @param component Logical component.
     * @param metricName Metric name (e.g. "outbox_synced_count", "sync_duration_ms").
     * @param value Numeric value.
     * @param attributes Optional dimensions.
     */
    fun recordMetric(component: String, metricName: String, value: Number, attributes: Map<String, Any?> = emptyMap())

    /**
     * Record a breadcrumb for tracing flow (e.g. "User tapped Create; navigating to form").
     * @param component Logical component.
     * @param message Short description.
     * @param category Optional category (e.g. "navigation", "sync").
     */
    fun recordBreadcrumb(component: String, message: String, category: String? = null)
}

@Singleton
class MonitorImpl @Inject constructor() : Monitor {

    private val tag = "Monitor"

    override fun recordEvent(component: String, eventName: String, attributes: Map<String, Any?>) {
        val attrs = if (attributes.isEmpty()) "" else " ${attributes.map { "${it.key}=${it.value}" }.joinToString(" ")}"
        Log.i(tag, "[$component] event=$eventName$attrs")
    }

    override fun recordMetric(component: String, metricName: String, value: Number, attributes: Map<String, Any?>) {
        val attrs = if (attributes.isEmpty()) "" else " ${attributes.map { "${it.key}=${it.value}" }.joinToString(" ")}"
        Log.i(tag, "[$component] metric=$metricName value=$value$attrs")
    }

    override fun recordBreadcrumb(component: String, message: String, category: String?) {
        val cat = if (category != null) " category=$category" else ""
        Log.i(tag, "[$component] breadcrumb=$message$cat")
    }
}
