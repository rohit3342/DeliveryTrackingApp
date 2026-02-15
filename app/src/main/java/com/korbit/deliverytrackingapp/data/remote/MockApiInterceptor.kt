package com.korbit.deliverytrackingapp.data.remote

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.IOException

/**
 * Mock interceptor for development: returns fake JSON for deliveries and task action.
 * Remove or disable in production.
 */
class MockApiInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.encodedPath
        val response = when {
            url.endsWith("deliveries") && request.method == "GET" -> buildDeliveriesResponse(request)
            url.contains("deliveries/") && request.method == "GET" -> buildSingleDeliveryResponse(request)
            url.endsWith("tasks/action") && request.method == "POST" -> buildTaskActionResponse(request)
            else -> null
        }
        return response ?: chain.proceed(request)
    }

    private fun buildDeliveriesResponse(request: okhttp3.Request): Response {
        val body = """
            [
              {"id":"d1","rider_id":"r1","status":"ACTIVE","customer_name":"Customer A","customer_address":"123 Main St","last_updated_at":${System.currentTimeMillis()},"tasks":[{"id":"t1","type":"PICKUP","status":"COMPLETED","sequence":1,"completed_at":${System.currentTimeMillis()}},{"id":"t2","type":"DELIVER","status":"PENDING","sequence":2,"completed_at":null}]},
              {"id":"d2","rider_id":"r1","status":"ACTIVE","customer_name":"Customer B","customer_address":"456 Oak Ave","last_updated_at":${System.currentTimeMillis()},"tasks":[{"id":"t3","type":"PICKUP","status":"PENDING","sequence":1,"completed_at":null}]}
            ]
        """.trimIndent()
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(body.toResponseBody("application/json".toMediaType()))
            .build()
    }

    private fun buildSingleDeliveryResponse(request: okhttp3.Request): Response {
        val body = """
            {"id":"d1","rider_id":"r1","status":"ACTIVE","customer_name":"Customer A","customer_address":"123 Main St","last_updated_at":${System.currentTimeMillis()},"tasks":[{"id":"t1","type":"PICKUP","status":"COMPLETED","sequence":1,"completed_at":${System.currentTimeMillis()}},{"id":"t2","type":"DELIVER","status":"PENDING","sequence":2,"completed_at":null}]}
        """.trimIndent()
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(body.toResponseBody("application/json".toMediaType()))
            .build()
    }

    private fun buildTaskActionResponse(request: okhttp3.Request): Response =
        Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body("{}".toResponseBody("application/json".toMediaType()))
            .build()
}
