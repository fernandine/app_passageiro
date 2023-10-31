package mobi.audax.tupi.passageiro.bin.controller.auth

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class BearerAuthorizationInterceptor(private val jwt: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val newRequest = chain.request().signedRequest()
        return chain.proceed(newRequest)
    }

    private fun Request.signedRequest(): Request {
        return newBuilder()
            .header("Authorization", "Bearer $jwt")
            .build()
    }
}