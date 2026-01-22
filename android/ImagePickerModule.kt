package com.ImagePickerApp.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.lynx.jsbridge.LynxMethod
import com.lynx.jsbridge.LynxModule
import com.lynx.react.bridge.Callback
import com.lynx.tasm.behavior.LynxContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.util.UUID

class ImagePickerModule(context: Context) : LynxModule(context) {

    companion object {
        private const val REQUEST_PICK = 9001
        private var current: ImagePickerModule? = null

        private var previewCallback: Callback? = null
        private var uploadCallback: Callback? = null
        private var uploadPath: String = ""
        private var authToken: String = ""

        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            current?.handleResult(requestCode, resultCode, data)
        }

        private const val SUPABASE_URL = "https://YOUR_PROJECT.supabase.co"
        private const val BUCKET = "photos"
        private const val API_KEY = "YOUR_PUBLIC_KEY"
    }

    init {
        current = this
    }

    private fun findActivity(): Activity? =
        when (val ctx = mContext) {
            is Activity -> ctx
            is LynxContext -> ctx.context as? Activity
            else -> null
        }

    @LynxMethod
    fun pickImages(
        accessToken: String,
        previewCb: Callback,
        uploadCb: Callback
    ) {
        authToken = accessToken
        previewCallback = previewCb
        uploadCallback = uploadCb

        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }

        findActivity()?.startActivityForResult(intent, REQUEST_PICK)
    }

    private fun handleResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != REQUEST_PICK || resultCode != Activity.RESULT_OK || data == null) {
            previewCallback?.invoke(null)
            return
        }

        val uris = mutableListOf<Uri>()

        data.clipData?.let {
            for (i in 0 until it.itemCount) {
                uris.add(it.getItemAt(i).uri)
            }
        }

        data.data?.let { uris.add(it) }

        if (uris.isEmpty()) {
            previewCallback?.invoke(null)
            return
        }

        previewCallback?.invoke(
            org.json.JSONArray(uris.map { it.toString() }).toString()
        )

        Thread {
            val uploaded = mutableListOf<String>()

            for (uri in uris) {
                upload(uri)?.let { uploaded.add(it) }
            }

            uploadCallback?.invoke(
                org.json.JSONArray(uploaded).toString()
            )
        }.start()
    }

    private fun upload(uri: Uri): String? {
        val bytes = mContext.contentResolver.openInputStream(uri)?.readBytes() ?: return null

        val filename = "images/${UUID.randomUUID()}.jpg"
        val body = RequestBody.create("image/jpeg".toMediaType(), bytes)

        val request = Request.Builder()
            .url("$SUPABASE_URL/storage/v1/object/$BUCKET/$filename")
            .addHeader("apikey", API_KEY)
            .addHeader("Authorization", "Bearer $authToken")
            .post(body)
            .build()

        val resp = OkHttpClient().newCall(request).execute()
        if (!resp.isSuccessful) return null

        return "$SUPABASE_URL/storage/v1/object/public/$BUCKET/$filename"
    }
}
