package mobi.audax.tupi.passageiro.bin.controller.retrofit

import mobi.audax.tupi.passageiro.bin.bean.Passageiro
import mobi.audax.tupi.passageiro.bin.bean.ViagemFrequente
import mobi.audax.tupi.passageiro.bin.dto.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface RotaPassageiroInterface {

    @GET("rota/passageiro/viagensFrequentes/{idPassageiro}")
    fun viagensFrequentes(@Path("idPassageiro") idPassageiro: Int?): Call<MutableList<ViagemFrequente>>

    @GET("rota/passageiro/paradas/proximas/{latitude}/{longitude}")
    fun paradasProximas(@Path("latitude") latitude : Double, @Path("longitude") long: Double): Call<MutableList<ParadaProxima>>

    @POST("rota/passageiro/paradas/destino")
    fun paradasDestino(@Body paradaDestino: ParadaDestino): Call<MutableList<ParadaEmbarque>>

    @GET("rota/passageiro/viagem/{id}")
    fun rotaViagem(@Path("id") id: Int): Call<Rota>

    @GET("rota/passageiro/ativa/{id}")
    fun rotaPassageiroAtiva(@Path("id") id: Int): Call<RotaAtiva>

    @GET("rota/passageiro/precoBase")
    fun precoBase(): Call<PrecoBase>

    @POST("rota/passageiro/finalizar")
    fun finalizar(@Body finalizarViagem: FinalizarViagem): Call<ResponseBody>

    @POST("rota/passageiro/tempoMotoristaParada")
    fun tempoMotoristaParada(@Body posicaoViagemRequest: PosicaoViagemRequest): Call<ParadaEmbarque>

    @POST("rota/passageiro/confirmarViagem")
    fun confirmarViagem(@Body confirmarViagem: ConfirmarViagem): Call<ResponseBody>

    @GET("rota/passageiro/cancelarViagem/{viagem}/{passageiro}")
    fun cancelarViagem(@Path("viagem") viagem: Int, @Path("passageiro") passageiro: Int): Call<ResponseBody>

}
