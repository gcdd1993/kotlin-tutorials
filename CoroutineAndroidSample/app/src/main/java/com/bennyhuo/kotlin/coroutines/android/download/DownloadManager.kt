package com.bennyhuo.kotlin.coroutines.android.download

import com.bennyhuo.kotlin.coroutines.android.api.HttpException
import com.bennyhuo.kotlin.coroutines.android.api.okHttpClient
import com.bennyhuo.kotlin.coroutines.android.appContext
import com.bennyhuo.kotlin.coroutines.android.utils.copyTo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flow
import okhttp3.Request
import java.io.File

object DownloadManager {

    private val downloadDirectory by lazy {
        File(appContext.filesDir, "download").also { it.mkdirs() }
    }

    sealed class DownloadStatus {
        object None : DownloadStatus()
        class Progress(val value: Int) : DownloadStatus()
        class Error(val throwable: Throwable) : DownloadStatus()
        class Done(val file: File) : DownloadStatus()
    }

    fun download(url: String, fileName: String): Flow<DownloadStatus> {
        val file = File(downloadDirectory, fileName)
        return flow {
            val request = Request.Builder().url(url).get().build()
            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                response.body()!!.let { body ->
                    val total = body.contentLength()
                    var emittedProgress = 0L
                    file.outputStream().use { output ->
                        body.byteStream().use { input ->
                            input.copyTo(output) { bytesCopied ->
                                val progress = bytesCopied * 100 / total
                                if (progress - emittedProgress > 5) {
                                    emit(DownloadStatus.Progress(progress.toInt()))
                                    emittedProgress = progress
                                }
                            }
                        }
                    }
                    emit(DownloadStatus.Done(file))
                }
            } else {
                throw HttpException(response)
            }
        }.catch {
            file.delete()
            emit(DownloadStatus.Error(it))
        }.conflate()
    }

}

