package com.example.coralpointreservo

import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object CloudinaryManager {

    private const val CLOUDINARY_CLOUD_NAME = "dlp2fugja"

    fun initialize(context: android.content.Context) {
        MediaManager.init(context, mapOf("cloud_name" to CLOUDINARY_CLOUD_NAME))
    }

    suspend fun uploadImage(uri: Uri): String? {
        return suspendCancellableCoroutine { continuation ->
            MediaManager.get().upload(uri)
                .option("folder", "receipt")
                .unsigned("CoralPoint")
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String) {}

                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        continuation.resume(resultData["secure_url"] as? String)
                    }

                    override fun onError(requestId: String, error: ErrorInfo) {
                        continuation.resume(null)
                    }

                    override fun onReschedule(requestId: String, error: ErrorInfo) {}
                })
                .dispatch()
        }
    }
}
