package com.korbit.deliverytrackingapp.data.remote

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

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
        val ts = System.currentTimeMillis()
        val body = """
            [
              {"id":"d1","rider_id":"r1","status":"ACTIVE","customer_name":"James Wilson","customer_address":"42 Oak Lane, Brooklyn, NY 11201","customer_phone":"+1 (212) 555-0147","last_updated_at":$ts,"tasks":[{"id":"t1","type":"PICKUP","status":"PENDING","sequence":1,"completed_at":null,"created_at":$ts,"last_modified_at":$ts}]},
              {"id":"d2","rider_id":"r1","status":"ACTIVE","customer_name":"Priya Sharma","customer_address":"1580 Commerce Drive, San Jose, CA 95131","customer_phone":"+1 (408) 555-0192","last_updated_at":${ts - 3600000},"tasks":[{"id":"t2","type":"PICKUP","status":"PICKED_UP","sequence":1,"completed_at":${ts - 3600000},"created_at":${ts - 7200000},"last_modified_at":${ts - 3600000}]},
              {"id":"d3","rider_id":"r1","status":"ACTIVE","customer_name":"Marcus Johnson","customer_address":"901 West Peachtree St, Atlanta, GA 30309","customer_phone":"+1 (404) 555-0234","last_updated_at":${ts - 7200000},"tasks":[{"id":"t3","type":"DELIVER","status":"REACHED","sequence":1,"completed_at":null,"created_at":${ts - 10800000},"last_modified_at":${ts - 7200000}]},
              {"id":"d4","rider_id":"r1","status":"ACTIVE","customer_name":"Elena Rodriguez","customer_address":"2200 N Loop West, Houston, TX 77018","customer_phone":"+1 (713) 555-0456","last_updated_at":${ts - 10800000},"tasks":[{"id":"t4","type":"DELIVER","status":"DELIVERED","sequence":1,"completed_at":${ts - 10800000},"created_at":${ts - 14400000},"last_modified_at":${ts - 10800000}]},
              {"id":"d5","rider_id":"r1","status":"ACTIVE","customer_name":"David Kim","customer_address":"5500 South Marginal Way, Seattle, WA 98134","customer_phone":"+1 (206) 555-0678","last_updated_at":${ts - 14400000},"tasks":[{"id":"t5","type":"DELIVER","status":"FAILED","sequence":1,"completed_at":null,"created_at":${ts - 18000000},"last_modified_at":${ts - 14400000}]},
              {"id":"d6","rider_id":"r1","status":"ACTIVE","customer_name":"Sophie Chen","customer_address":"100 Market St, San Francisco, CA 94105","customer_phone":"+1 (415) 555-0321","last_updated_at":${ts - 18000000},"tasks":[{"id":"t6","type":"PICKUP","status":"PENDING","sequence":1,"completed_at":null,"created_at":${ts - 18000000},"last_modified_at":${ts - 18000000}]},
              {"id":"d7","rider_id":"r1","status":"ACTIVE","customer_name":"Omar Hassan","customer_address":"3300 S Las Vegas Blvd, Las Vegas, NV 89109","customer_phone":"+1 (702) 555-0890","last_updated_at":${ts - 21600000},"tasks":[{"id":"t7","type":"DELIVER","status":"FAILED","sequence":1,"completed_at":null,"created_at":${ts - 21600000},"last_modified_at":${ts - 21600000}]}
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
        val ts = System.currentTimeMillis()
        val body = """
            {"id":"d1","rider_id":"r1","status":"ACTIVE","customer_name":"James Wilson","customer_address":"42 Oak Lane, Brooklyn, NY 11201","customer_phone":"+1 (212) 555-0147","last_updated_at":$ts,"tasks":[{"id":"t1","type":"PICKUP","status":"PENDING","sequence":1,"completed_at":null,"created_at":$ts,"last_modified_at":$ts}]}
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
