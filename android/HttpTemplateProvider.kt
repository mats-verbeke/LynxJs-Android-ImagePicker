package com.ImagePickerApp.app

import com.lynx.tasm.provider.AbsTemplateProvider
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.Executors

class HttpTemplateProvider : AbsTemplateProvider() {

    private val client = OkHttpClient()
    private val executor = Executors.newSingleThreadExecutor()

    override fun loadTemplate(uri: String, callback: Callback) {
        executor.execute {
            try {
                val req = Request.Builder().url(uri).build()
                val resp = client.newCall(req).execute()
                val bytes = resp.body?.bytes()

                if (bytes != null) callback.onSuccess(bytes)
                else callback.onFailed("Empty response")

            } catch (e: Exception) {
                callback.onFailed(e.message)
            }
        }
    }
}
