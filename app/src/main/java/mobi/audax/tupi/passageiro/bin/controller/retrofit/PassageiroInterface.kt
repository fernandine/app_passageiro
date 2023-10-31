package mobi.audax.tupi.passageiro.bin.controller.retrofit

import mobi.audax.tupi.passageiro.bin.bean.Cartao
import mobi.audax.tupi.passageiro.bin.bean.Passenger
import mobi.audax.tupi.passageiro.bin.dto.GoogleAuth
import mobi.audax.tupi.passageiro.bin.dto.Hash
import mobi.audax.tupi.passageiro.bin.dto.Login
import mobi.audax.tupi.passageiro.bin.dto.PassageiroUpdateRequest
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface PassageiroInterface {

    @POST("passageiro/autenticacao/login")
    fun login(@Body login: Login): Call<Passenger>

    @POST("passageiro/autenticacao/loginGoogle")
    fun googleAuth(@Body auth: GoogleAuth): Call<Passenger>

    @PUT("passageiro/autenticacao/refreshToken")
    fun refreshToken(@Body hash: Hash): Call<Passenger>

    @GET("passageiro/autenticacao/recuperar/senha/{email}")
    fun recuperarSenha(@Path("email") email: String): Call<ResponseBody>

    @POST("passageiro")
    fun create(@Body passenger: Passenger): Call<ResponseBody>

    @PUT("passageiro")
    fun update(@Body passenger: PassageiroUpdateRequest): Call<Passenger>

    @PUT("passageiro/paymentMethod/{id}/{hash}")
    fun updatePaymentMethod(@Path("id") id: Int, @Path("hash") hash: String, @Body cartao: Cartao): Call<ResponseBody>

}
