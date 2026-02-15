package com.korbit.deliverytrackingapp.data.remote

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.korbit.deliverytrackingapp.data.remote.dto.DeliveryDto
import com.korbit.deliverytrackingapp.data.remote.dto.TaskActionRequestDto
import com.korbit.deliverytrackingapp.data.remote.dto.TaskDto
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import java.util.concurrent.ConcurrentHashMap

/**
 * Mock interceptor for development: returns fake JSON for deliveries and task action.
 * Keeps in-memory state for task actions and created deliveries (POST /deliveries).
 * Remove or disable in production.
 */
class MockApiInterceptor : Interceptor {

    private val gson = Gson()
    /** taskId -> (status, lastModifiedAt) applied via POST /tasks/action */
    private val taskActionOverrides = ConcurrentHashMap<String, Pair<String, Long>>()
    /** deliveryId -> DeliveryDto created via POST /deliveries (createTask) */
    private val createdDeliveries = ConcurrentHashMap<String, DeliveryDto>()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.encodedPath
        val response = when {
            url.endsWith("deliveries") && request.method == "GET" -> buildDeliveriesResponse(request)
            url.endsWith("tasks/create") && request.method == "POST" -> handleCreateTasks(request)
            url.contains("deliveries/") && request.method == "GET" -> buildSingleDeliveryResponse(request)
            url.endsWith("tasks/action") && request.method == "POST" -> buildTaskActionResponse(request)
            else -> null
        }
        return response ?: chain.proceed(request)
    }

    private fun handleCreateTasks(request: okhttp3.Request): Response {
        request.body?.let { body ->
            val source = Buffer().apply { body.writeTo(this) }
            val json = source.readUtf8()
            runCatching {
                val type = object : TypeToken<List<DeliveryDto>>() {}.type
                @Suppress("UNCHECKED_CAST")
                val list = gson.fromJson<List<DeliveryDto>>(json, type) ?: emptyList()
                list.forEach { dto -> createdDeliveries[dto.id] = dto }
            }
        }
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body("{}".toResponseBody("application/json".toMediaType()))
            .build()
    }

    private fun buildDeliveriesResponse(request: okhttp3.Request): Response {
        val ts = System.currentTimeMillis()
        val baseDeliveries = listOf(
            DeliveryDto("d1", "r1", "ACTIVE", "James Wilson", "42 Oak Lane, Brooklyn, NY 11201", "+1 (212) 555-0147", "Main Warehouse", "100 Industrial Parkway", ts, listOf(TaskDto("t1", "PICKUP", "PENDING", 1, null, ts, ts))),
            DeliveryDto("d2", "r1", "ACTIVE", "Priya Sharma", "1580 Commerce Drive, San Jose, CA 95131", "+1 (408) 555-0192", "Main Warehouse", "100 Industrial Parkway", ts - 3600000L, listOf(TaskDto("t2", "PICKUP", "PICKED_UP", 1, ts - 3600000L, ts - 7200000L, ts - 3600000L))),
            DeliveryDto("d3", "r1", "ACTIVE", "Marcus Johnson", "901 West Peachtree St, Atlanta, GA 30309", "+1 (404) 555-0234", "North Warehouse", "250 Commerce Drive", ts - 7200000L, listOf(TaskDto("t3", "DELIVER", "REACHED", 1, null, ts - 10800000L, ts - 7200000L))),
            DeliveryDto("d4", "r1", "ACTIVE", "Elena Rodriguez", "2200 N Loop West, Houston, TX 77018", "+1 (713) 555-0456", "Main Warehouse", "100 Industrial Parkway", ts - 10800000L, listOf(TaskDto("t4", "DELIVER", "DELIVERED", 1, ts - 10800000L, ts - 14400000L, ts - 10800000L))),
            DeliveryDto("d5", "r1", "ACTIVE", "David Kim", "5500 South Marginal Way, Seattle, WA 98134", "+1 (206) 555-0678", "West Warehouse", "5500 South Marginal Way", ts - 14400000L, listOf(TaskDto("t5", "DELIVER", "FAILED", 1, null, ts - 18000000L, ts - 14400000L))),
            DeliveryDto("d6", "r1", "ACTIVE", "Sophie Chen", "100 Market St, San Francisco, CA 94105", "+1 (415) 555-0321", "Main Warehouse", "100 Industrial Parkway", ts - 18000000L, listOf(TaskDto("t6", "PICKUP", "PENDING", 1, null, ts - 18000000L, ts - 18000000L))),
            DeliveryDto("d7", "r1", "ACTIVE", "Omar Hassan", "3300 S Las Vegas Blvd, Las Vegas, NV 89109", "+1 (702) 555-0890", "South Warehouse", "3300 S Las Vegas Blvd", ts - 21600000L, listOf(TaskDto("t7", "DELIVER", "FAILED", 1, null, ts - 21600000L, ts - 21600000L)))
        )
        val allBase = baseDeliveries + createdDeliveries.values.toList()
        val deliveries = allBase.map { d ->
            val overriddenTasks = d.tasks?.map { t ->
                taskActionOverrides[t.id]?.let { (status, modifiedAt) ->
                    t.copy(status = status, lastModifiedAt = modifiedAt, completedAt = if (status in listOf("DELIVERED", "FAILED", "PICKED_UP", "REACHED")) modifiedAt else t.completedAt)
                } ?: t
            }
            d.copy(tasks = overriddenTasks)
        }
        val body = gson.toJson(deliveries)
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(body.toResponseBody("application/json".toMediaType()))
            .build()
    }

    private fun buildSingleDeliveryResponse(request: okhttp3.Request): Response {
        val ts = System.currentTimeMillis()
        val base = DeliveryDto("d1", "r1", "ACTIVE", "James Wilson", "42 Oak Lane, Brooklyn, NY 11201", "+1 (212) 555-0147", "Main Warehouse", "100 Industrial Parkway", ts, listOf(TaskDto("t1", "PICKUP", "PENDING", 1, null, ts, ts)))
        val overriddenTasks = base.tasks?.map { t ->
            taskActionOverrides[t.id]?.let { (status, modifiedAt) ->
                t.copy(status = status, lastModifiedAt = modifiedAt, completedAt = if (status in listOf("DELIVERED", "FAILED", "PICKED_UP", "REACHED")) modifiedAt else t.completedAt)
            } ?: t
        }
        val delivery = base.copy(tasks = overriddenTasks)
        val body = gson.toJson(delivery)
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(body.toResponseBody("application/json".toMediaType()))
            .build()
    }

    private fun buildTaskActionResponse(request: okhttp3.Request): Response {
        request.body?.let { body ->
            val source = Buffer().apply { body.writeTo(this) }
            val json = source.readUtf8()
            runCatching {
                val type = object : TypeToken<List<TaskActionRequestDto>>() {}.type
                @Suppress("UNCHECKED_CAST")
                val list = gson.fromJson<List<TaskActionRequestDto>>(json, type) ?: emptyList()
                list.forEach { dto ->
                    val actionTakenAt = dto.actionTakenAt ?: System.currentTimeMillis()
                    taskActionOverrides[dto.taskId] = dto.action to actionTakenAt
                }
            }
        }
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body("{}".toResponseBody("application/json".toMediaType()))
            .build()
    }
}
