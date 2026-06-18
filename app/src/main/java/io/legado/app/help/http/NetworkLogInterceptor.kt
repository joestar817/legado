package io.legado.app.help.http

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

object NetworkLogInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (!NetworkLog.isEnabled) {
            return chain.proceed(request)
        }
        val start = System.nanoTime()
        return try {
            val response = chain.proceed(request)
            NetworkLog.recordOkHttp(
                request = request,
                response = response,
                tookMs = elapsedMs(start)
            )
            response
        } catch (e: IOException) {
            NetworkLog.recordOkHttp(
                request = request,
                response = null,
                tookMs = elapsedMs(start),
                error = e
            )
            throw e
        } catch (e: Throwable) {
            NetworkLog.recordOkHttp(
                request = request,
                response = null,
                tookMs = elapsedMs(start),
                error = e
            )
            throw e
        }
    }

    private fun elapsedMs(start: Long): Long {
        return (System.nanoTime() - start) / 1_000_000L
    }
}
