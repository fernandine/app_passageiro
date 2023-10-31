package mobi.audax.tupi.passageiro.bin.controller

import android.content.Context
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import mobi.audax.tupi.passageiro.R
import mobi.audax.tupi.passageiro.bin.bo.PassengerBo
import mobi.audax.tupi.passageiro.bin.controller.auth.BearerAuthorizationInterceptor
import mobi.audax.tupi.passageiro.bin.controller.retrofit.MapsApiInterface
import mobi.audax.tupi.passageiro.bin.dto.AutoSuggest
import mobi.audax.tupi.passageiro.bin.util.Prefs
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.apache.commons.lang3.StringUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit

class MapsApiController(override val context: Context) : CommonsController(context) {

    private fun mapsRetrofit(): Retrofit {
        val logging = HttpLoggingInterceptor()
        if (context.resources.getBoolean(R.bool.debug)) {
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        }

        val builder = GsonBuilder()
        builder.registerTypeAdapter(Date::class.java, JsonDeserializer { json, _, _ -> Date(json.asJsonPrimitive.asLong) })
        builder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss-0300")
        builder.excludeFieldsWithoutExposeAnnotation()

        val httpClient = OkHttpClient.Builder()
        httpClient.addInterceptor(logging)
        httpClient.readTimeout(120, TimeUnit.SECONDS)
        httpClient.connectTimeout(120, TimeUnit.SECONDS)

        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(builder.create()))
            .baseUrl(context.getString(R.string.base_urlHereMaps))
            .client(httpClient.build())
            .build()
    }

    fun autoSuggest(query: String, onBefore: () -> Unit, onComplete: (Int, AutoSuggest?) -> Unit) {
        val lat = Prefs(context).latitude.toDouble()
        val lng = Prefs(context).longitude.toDouble()

        super.onBeforeExecute(onBefore)
        val service = mapsRetrofit().create(MapsApiInterface::class.java)
        val call = service.autoSuggest("$lat,$lng", StringUtils.trimToEmpty(query))
        call.enqueue(object : Callback<AutoSuggest?> {
            override fun onResponse(call: Call<AutoSuggest?>, response: Response<AutoSuggest?>) {
                onComplete(response.code(), response.body())
            }

            override fun onFailure(call: Call<AutoSuggest?>, t: Throwable) {
                onComplete(999, null)
            }
        })
    }

}
