package com.yumenonaka.yumenoschedule.apis

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class YumenoApis {
    companion object {
        private val client: OkHttpClient = OkHttpClient()
        private const val baseApiUrl: String = "https://www.yumeno-naka.moe/yumeno_api"

        fun getRecentSchedule(): String {
            val request: Request = Request.Builder()
                .url("$baseApiUrl/recentEvent?useBr=true")
                .build()
            val response: Response = client.newCall(request).execute()
            return response.body!!.string()
        }
    }
}
