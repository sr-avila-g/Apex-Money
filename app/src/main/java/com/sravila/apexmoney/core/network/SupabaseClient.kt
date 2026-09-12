package com.sravila.apexmoney.core.network

import com.sravila.apexmoney.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseApi {
    private var instance: io.github.jan.supabase.SupabaseClient? = null
    private var lastUrl = ""
    private var lastKey = ""

    fun getClient(customUrl: String, customKey: String): io.github.jan.supabase.SupabaseClient {
        val targetUrl = if (customUrl.isNotBlank()) customUrl else BuildConfig.SUPABASE_URL
        val targetKey = if (customKey.isNotBlank()) customKey else BuildConfig.SUPABASE_ANON_KEY

        if (instance != null && lastUrl == targetUrl && lastKey == targetKey) {
            return instance!!
        }

        instance = createSupabaseClient(
            supabaseUrl = targetUrl,
            supabaseKey = targetKey
        ) {
            install(Postgrest)
        }
        lastUrl = targetUrl
        lastKey = targetKey
        return instance!!
    }
}
