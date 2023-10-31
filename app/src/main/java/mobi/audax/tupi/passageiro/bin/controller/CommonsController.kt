package mobi.audax.tupi.passageiro.bin.controller

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mobi.audax.tupi.passageiro.R
import mobi.audax.tupi.passageiro.activities.login.LoginActivity
import mobi.audax.tupi.passageiro.bin.bo.PassengerBo
import mobi.audax.tupi.passageiro.bin.controller.auth.BearerAuthorizationInterceptor
import mobi.audax.tupi.passageiro.bin.util.Util
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit

abstract class CommonsController(open val context: Context) {
    private var tryRefreshToken = false

    var hash: String
        get() {
            PassengerBo(context).autenticado()?.let {
                it.hash?.let { text ->
                    return text
                }
            }
            return ""
        }
        set(value) {}

    var jwt: String
        get() {
            PassengerBo(context).autenticado()?.let {
                it.token?.let { text ->
                    return text
                }
            }
            return ""
        }
        set(value) {}

    var id: Int
        get() {
            PassengerBo(context).autenticado()?.let {
                return it.id
            }
            return 0
        }
        set(value) {}

    var rest: String
        get() {
            return context.getString(R.string.base_url)
        }
        set(value) {}


    var retrofit: Retrofit
        get() {
            val logging = HttpLoggingInterceptor()
            if (context.resources.getBoolean(R.bool.debug)) {
                logging.setLevel(HttpLoggingInterceptor.Level.BODY)
            }

            val builder = GsonBuilder()
            builder.registerTypeAdapter(Date::class.java, JsonDeserializer { json, typeOfT, context -> Date(json.asJsonPrimitive.asLong) })
            builder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss-0300")
            builder.excludeFieldsWithoutExposeAnnotation()

            val httpClient = OkHttpClient.Builder()
            httpClient.addInterceptor(logging)
            httpClient.addInterceptor(BearerAuthorizationInterceptor(jwt))
            httpClient.readTimeout(120, TimeUnit.SECONDS)
            httpClient.connectTimeout(120, TimeUnit.SECONDS)
//            httpClient.authenticator()
            return Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(builder.create()))
                .baseUrl(rest)
                .client(httpClient.build())
                .build()
        }
        set(value) {}

    fun onBeforeExecute(onBefore: () -> Unit) {
        val job = CoroutineScope(Dispatchers.Main).launch {
            kotlin.run {
                onBefore()
            }
        }
        job.start()
    }

    protected fun tryRefreshToken(fn: () -> Unit) {
        if (!this.tryRefreshToken) {
            this.tryRefreshToken = true
            PassageiroController(context).refreshToken {
                fn()
            }
        } else {
            if (context is AppCompatActivity) {
                Util.loginExpirado(context as AppCompatActivity) {
                    this.forceLogout()
                }
            } else {
               this.forceLogout()
            }
        }
    }

    private fun forceLogout() {
        PassengerBo(context).logout()
        context.startActivity(Intent(context, LoginActivity::class.java))
    }

}